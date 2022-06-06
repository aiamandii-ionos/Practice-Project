package com.ionos.project.service;

import com.ionos.project.model.Server;
import com.ionos.project.repository.ServerRepository;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import javax.persistence.PersistenceException;
import javax.ws.rs.NotFoundException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServerServiceTest {
    @InjectMocks
    ServerService service;

    @Mock
    ServerRepository repository;

    @Mock
    Logger logger;

    @Mock
    JsonWebToken jwt;

    @Mock
    SecurityIdentity securityIdentity;

    @Test
    void saveServer_Success() {
        UUID uuid = UUID.randomUUID();

        Mockito.when(jwt.getSubject()).thenReturn(String.valueOf(uuid));

        Server toBeSavedServer = Server.builder()
                .userId(uuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();

        doNothing().when(repository).persist(toBeSavedServer);
        Server result = service.save(toBeSavedServer);
        assertThat(result).isEqualTo(toBeSavedServer);
    }

    @Test
    void saveServer_Failure() {
        UUID uuid = UUID.randomUUID();

        Server toBeSavedServer = Server.builder()
                .userId(uuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();

        Mockito.when(jwt.getSubject()).thenReturn(String.valueOf(uuid));

        doThrow(new PersistenceException()).when(repository).persist(toBeSavedServer);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.save(toBeSavedServer);
        });
        assertNotNull(exception);
    }

    @Test
    void getAllServers() {
        UUID uuid = UUID.randomUUID();
        Server toBeSavedServer = Server.builder()
                .userId(uuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();

//        PanacheQuery query = Mockito.mock(PanacheQuery.class);
//        Mockito.when(query.list()).thenReturn(List.of(toBeSavedServer));

        Mockito.when(repository.getAll())
                .thenReturn(List.of(toBeSavedServer));

        // When
        List<Server> result = service.findAll();

        // Then
        verify(repository, times(1)).getAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("server1");
    }

    @Test
    void getServerById_Success() {
        UUID uuid = UUID.randomUUID();
        Server server = Server.builder()
                .id(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))
                .userId(uuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();

        Mockito.when(securityIdentity.hasRole("admin")).thenReturn(false);
        Mockito.when(jwt.getSubject()).thenReturn(String.valueOf(uuid));
        Mockito.when(repository.findByIdOptional(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))).thenReturn(Optional.ofNullable(server));

        Server serverGot = service.findById(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"));

        assertThat(serverGot).isEqualTo(server);
    }

    @Test
    void getServerById_Failure() {
        UUID uuid = UUID.randomUUID();

        Mockito.when(repository.findByIdOptional(any(UUID.class))).thenThrow(new NotFoundException());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.findById(uuid);
        });
        assertNotNull(exception);
    }

    @Test
    void getServerById_FailurePermission() {
        UUID uuid = UUID.randomUUID();
        Server server = Server.builder()
                .id(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))
                .userId(uuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();

        Mockito.when(securityIdentity.hasRole("admin")).thenThrow(new NotFoundException());
        Mockito.when(jwt.getSubject()).thenReturn(String.valueOf(uuid));

        Mockito.when(repository.findByIdOptional(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))).thenReturn(Optional.ofNullable(server));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.findById(uuid);
        });
        assertNotNull(exception);
    }

    @Test
    void deleteServer_Success() {
        UUID uuid = UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae");
        UUID userUuid = UUID.randomUUID();

        Server server = Server.builder()
                .id(uuid)
                .userId(userUuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();

        Mockito.when(securityIdentity.hasRole("admin")).thenReturn(false);
        Mockito.when(jwt.getSubject()).thenReturn(String.valueOf(userUuid));
        Mockito.when(repository.findByIdOptional(uuid)).thenReturn(Optional.ofNullable(server));
        Mockito.when(repository.deleteById(uuid)).thenReturn(true);

        service.delete(uuid);
        verify(repository, times(1)).deleteById(uuid);
    }

    @Test
    void deleteServer_Failure() {
        UUID uuid = UUID.randomUUID();

        Mockito.when(repository.findByIdOptional(uuid)).thenThrow(new NotFoundException());

        Exception exception = assertThrows(NotFoundException.class, () -> {
            service.delete(uuid);
        });
        assertNotNull(exception);
    }

    @Test
    void updateServer_Success() {
        UUID uuid = UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae");
        UUID userUuid = UUID.randomUUID();

        Server oldServer = Server.builder()
                .id(uuid)
                .userId(userUuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();

        Server newServer = Server.builder()
                .id(uuid)
                .userId(userUuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server2").build();

        Mockito.when(securityIdentity.hasRole("admin")).thenReturn(false);
        Mockito.when(jwt.getSubject()).thenReturn(String.valueOf(userUuid));

        Mockito.when(repository.findByIdOptional(uuid)).thenReturn(Optional.ofNullable(oldServer));
        doNothing().when(repository).persist(newServer);

        Server updated = service.update(uuid, newServer);
        assertThat(updated).isEqualTo(newServer);
    }

    @Test
    void updateServer_Failure() {
        UUID uuid = UUID.randomUUID();
        Server newServer = Server.builder()
                .id(uuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server2").build();

        Mockito.when(repository.findByIdOptional(uuid)).thenThrow(new NotFoundException());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.update(uuid, newServer);
        });
        assertNotNull(exception);
    }
}
