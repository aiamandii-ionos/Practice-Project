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

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

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

        String properties =  "{\"name\":\"s\",\"cores\":2,\"ram\":1024,\"storage\":3}";


        doNothing().when(requestRepository).persist(any(Request.class));

        Request result = requestService.createRequest(RequestType.CREATE_SERVER, toBeSavedServer, null);
        assertEquals(result.getType(), RequestType.CREATE_SERVER);
        assertEquals(result.getStatus(), RequestStatus.TO_DO);
        assertEquals(result.getProperties(), properties);
        assertEquals(result.getUserId(), uuid);
    }

    @Test
    void createUpdateRequest_Success(){
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

        String properties =  "{\"name\":\"s\",\"cores\":2,\"ram\":1024,\"storage\":3}";

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
    void createDeleteRequest_Success(){
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
}
