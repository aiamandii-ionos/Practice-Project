package com.ionos.project.service;

import com.ionos.project.model.*;
import com.ionos.project.model.enums.*;
import com.ionos.project.repository.*;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import javax.ws.rs.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {

    @Mock
    ServerService serverService;

    @InjectMocks
    RequestService requestService;

    @Mock
    RequestRepository requestRepository;

    @Mock
    ServerRepository repository;

    @Mock
    Logger logger;

    @Mock
    JsonWebToken jwt;

    @Mock
    SecurityIdentity securityIdentity;


    @Test
    void createServerRequest_Success() {
        UUID uuid = UUID.randomUUID();

        Server toBeSavedServer = Server.builder()
                .cores(2)
                .ram(1024)
                .storage(3)
                .name("s").build();

        Mockito.when(jwt.getSubject()).thenReturn(String.valueOf(uuid));

        String properties = "{\"name\":\"s\",\"cores\":2,\"ram\":1024,\"storage\":3}";


        doNothing().when(requestRepository).persist(any(Request.class));

        Request result = requestService.createRequest(RequestType.CREATE_SERVER, toBeSavedServer, null);
        assertEquals(result.getType(), RequestType.CREATE_SERVER);
        assertEquals(result.getStatus(), RequestStatus.TO_DO);
        assertEquals(result.getProperties(), properties);
        assertEquals(result.getUserId(), uuid);
    }

    @Test
    void updateServerRequest_ThrowsNotFoundException() {
        UUID uuid = UUID.randomUUID();

        Server toBeSavedServer = Server.builder()
                .cores(2)
                .ram(1024)
                .storage(3)
                .name("s").build();

        Mockito.when(jwt.getSubject()).thenReturn(String.valueOf(uuid));

        Mockito.when(repository.findByIdOptional(uuid)).thenThrow(new NotFoundException());

        Exception exception = assertThrows(RuntimeException.class, () -> requestService.createRequest(RequestType.UPDATE_SERVER, toBeSavedServer, uuid));
        assertNotNull(exception);
    }

    @Test
    void deleteServerRequest_ThrowsNotFoundException() {
        UUID uuid = UUID.randomUUID();

        Mockito.when(jwt.getSubject()).thenReturn(String.valueOf(uuid));
        Mockito.when(repository.findByIdOptional(uuid)).thenThrow(new NotFoundException());

        Exception exception = assertThrows(RuntimeException.class, () -> requestService.createRequest(RequestType.DELETE_SERVER, null, uuid));
        assertNotNull(exception);
    }

    @Test
    void createUpdateRequest_Success() {
        UUID uuid = UUID.randomUUID();

        String id = "a848a45e-d065-11ec-a62f-2d718d2fcfae";
        Server updatedServer = Server.builder()
                .cores(2)
                .ram(1024)
                .storage(3)
                .name("s").build();


        Server serverToUpdate = Server.builder()
                .id(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))
                .userId(uuid)
                .cores(2)
                .ram(1024)
                .storage(30)
                .name("server1").build();

        String properties = "{\"name\":\"s\",\"cores\":2,\"ram\":1024,\"storage\":3}";

        Mockito.when(securityIdentity.hasRole("admin")).thenReturn(false);
        Mockito.when(jwt.getSubject()).thenReturn(String.valueOf(uuid));
        Mockito.when(repository.findByIdOptional(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))).thenReturn(Optional.ofNullable(serverToUpdate));

        doNothing().when(requestRepository).persist(any(Request.class));

        Request result = requestService.createRequest(RequestType.UPDATE_SERVER, updatedServer, UUID.fromString(id));
        assertEquals(result.getType(), RequestType.UPDATE_SERVER);
        assertEquals(result.getStatus(), RequestStatus.TO_DO);
        assertEquals(result.getProperties(), properties);
        assertEquals(result.getUserId(), uuid);
        assertEquals(result.getServer().getId(), UUID.fromString(id));
    }

    @Test
    void createDeleteRequest_Success() {
        UUID uuid = UUID.randomUUID();

        String id = "a848a45e-d065-11ec-a62f-2d718d2fcfae";


        Server serverToDelete = Server.builder()
                .id(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))
                .userId(uuid)
                .cores(2)
                .ram(1024)
                .storage(3)
                .name("s").build();

        Mockito.when(securityIdentity.hasRole("admin")).thenReturn(false);
        Mockito.when(jwt.getSubject()).thenReturn(String.valueOf(uuid));
        Mockito.when(repository.findByIdOptional(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))).thenReturn(Optional.ofNullable(serverToDelete));


        doNothing().when(requestRepository).persist(any(Request.class));

        Request result = requestService.createRequest(RequestType.DELETE_SERVER, null, UUID.fromString(id));
        assertEquals(result.getType(), RequestType.DELETE_SERVER);
        assertEquals(result.getStatus(), RequestStatus.TO_DO);
        assertEquals(result.getUserId(), uuid);
        assertEquals(result.getServer().getId(), UUID.fromString(id));
    }

    @Test
    void scheduleCreateRequest_Success(){
        UUID uuid = UUID.randomUUID();
        Server server = Server.builder()
                .id(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))
                .userId(uuid)
                .cores(2)
                .ram(1024)
                .storage(3)
                .name("s").build();

        String properties = "{\"name\":\"s\",\"cores\":2,\"ram\":1024,\"storage\":3}";

        Request request = Request.builder().type(RequestType.CREATE_SERVER).requestId(uuid)
                .message("").properties(properties).status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).server(server).userId(uuid).build();
        Mockito.when(requestRepository.getLastRequest()).thenReturn(request);

        Mockito.when(requestRepository.findById(uuid)).thenReturn(request);
        Mockito.when(serverService.create(any(Server.class), any())).thenReturn(server);

        doNothing().when(requestRepository).persist(request);
        requestService.scheduleRequests();
        assertEquals(request.getStatus(), RequestStatus.DONE);
        assertEquals(request.getServer().getId(), server.getId());
        assertEquals(request.getServer().getName(), server.getName());
        assertEquals(request.getType(), request.getType());
        assertEquals(request.getMessage(), request.getMessage());
    }

    @Test
    void scheduleUpdateRequest_Success(){
        UUID uuid = UUID.randomUUID();
        Server server = Server.builder()
                .id(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))
                .userId(uuid)
                .cores(2)
                .ram(1024)
                .storage(3)
                .name("s").build();

        String properties = "{\"name\":\"s\",\"cores\":2,\"ram\":1024,\"storage\":3}";

        Request request = Request.builder().type(RequestType.UPDATE_SERVER).requestId(uuid)
                .message("").properties(properties).status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).server(server).userId(uuid).build();
        Mockito.when(requestRepository.getLastRequest()).thenReturn(request);

        Mockito.when(requestRepository.findById(uuid)).thenReturn(request);
        Mockito.when(serverService.update(any(), any(Server.class))).thenReturn(server);

        doNothing().when(requestRepository).persist(request);
        requestService.scheduleRequests();
        assertEquals(request.getStatus(), RequestStatus.DONE);
        assertEquals(request.getServer().getId(), server.getId());
        assertEquals(request.getServer().getName(), server.getName());
        assertEquals(request.getType(), request.getType());
        assertEquals(request.getMessage(), request.getMessage());
    }

    @Test
    void scheduleDeleteRequest_Success(){
        UUID uuid = UUID.randomUUID();
        Server server = Server.builder()
                .id(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))
                .userId(uuid)
                .cores(2)
                .ram(1024)
                .storage(3)
                .name("s").build();

        String properties = "{\"name\":\"s\",\"cores\":2,\"ram\":1024,\"storage\":3}";

        Request request = Request.builder().type(RequestType.DELETE_SERVER).requestId(uuid)
                .message("").properties(properties).status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).server(server).userId(uuid).build();
        Mockito.when(requestRepository.getLastRequest()).thenReturn(request);

        Mockito.when(requestRepository.findById(uuid)).thenReturn(request);
        Mockito.when(requestRepository.findRequestsByServerId(any())).thenReturn(List.of(request));
        doNothing().when(serverService).delete(any());

        doNothing().when(requestRepository).persist(request);
        requestService.scheduleRequests();
        assertEquals(request.getStatus(), RequestStatus.DONE);
        assertNull(request.getServer());
        assertEquals(request.getType(), request.getType());
        assertEquals(request.getMessage(), request.getMessage());
    }

    @Test
    void getAllRequests_Success() {
        UUID uuid = UUID.randomUUID();
        Server server = Server.builder()
                .id(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))
                .userId(uuid)
                .cores(2)
                .ram(1024)
                .storage(3)
                .name("s").build();

        String properties = "{\"name\":\"s\",\"cores\":2,\"ram\":1024,\"storage\":3}";
        Request request = Request.builder().type(RequestType.DELETE_SERVER).requestId(uuid)
                .message("").properties(properties).status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).server(server).userId(uuid).build();

        Mockito.when(requestRepository.getAll())
                .thenReturn(List.of(request));

        List<Request> result = requestService.findAll();

        verify(requestRepository, times(1)).getAll();
        assertThat(result).hasSize(1);
        assertEquals(result.get(0).getType(), RequestType.DELETE_SERVER);
    }

    @Test
    void getRequestById_Success() {
        UUID uuid = UUID.randomUUID();
        Server server = Server.builder()
                .id(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))
                .userId(uuid)
                .cores(2)
                .ram(1024)
                .storage(3)
                .name("s").build();

        String properties = "{\"name\":\"s\",\"cores\":2,\"ram\":1024,\"storage\":3}";
        Request request = Request.builder().type(RequestType.DELETE_SERVER).requestId(uuid)
                .message("").properties(properties).status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).server(server).userId(uuid).build();

        Mockito.when(securityIdentity.hasRole("admin")).thenReturn(false);
        Mockito.when(jwt.getSubject()).thenReturn(String.valueOf(uuid));
        Mockito.when(requestRepository.findByIdOptional(uuid)).thenReturn(Optional.ofNullable(request));

        Request requestGot = requestService.findById(uuid);

        assertThat(requestGot).isEqualTo(request);
    }

    @Test
    void getRequestById_ThrowsNotFoundException() {
        UUID uuid = UUID.randomUUID();

        Mockito.when(requestRepository.findByIdOptional(any(UUID.class))).thenThrow(new NotFoundException());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            requestService.findById(uuid);
        });
        assertNotNull(exception);
    }

    @Test
    void getRequestById_PermissionDenied() {
        UUID uuid = UUID.randomUUID();
        Server server = Server.builder()
                .id(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))
                .userId(uuid)
                .cores(2)
                .ram(1024)
                .storage(3)
                .name("s").build();

        String properties = "{\"name\":\"s\",\"cores\":2,\"ram\":1024,\"storage\":3}";
        Request request = Request.builder().type(RequestType.DELETE_SERVER).requestId(uuid)
                .message("").properties(properties).status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).server(server).userId(uuid).build();

        Mockito.when(securityIdentity.hasRole("admin")).thenThrow(new NotFoundException());

        Mockito.when(requestRepository.findByIdOptional(uuid)).thenReturn(Optional.ofNullable(request));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            requestService.findById(uuid);
        });
        assertNotNull(exception);
    }


}
