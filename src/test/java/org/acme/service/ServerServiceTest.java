package org.acme.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.acme.model.Server;
import org.acme.repository.ServerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class ServerServiceTest {
    @InjectMock
    ServerService service;

    @InjectMock
    ServerRepository repository;

    @Test
    void saveServer_Success(){
        Server savedServer = Server.builder()
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();

        Mockito.when(service.save(any(Server.class))).thenReturn(savedServer);

        Server toBeSavedServer = Server.builder()
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();
        Server result = service.save(toBeSavedServer);
        assertThat(result).isEqualTo(toBeSavedServer);
    }

    @Test
    void saveServer_Failure(){
        Server toBeSavedServer = Server.builder()
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();

        Mockito.when(service.save(any(Server.class))).thenThrow(new RuntimeException());
        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.save(toBeSavedServer);
        });
        assertNotNull(exception);
    }

    @Test
    void getAllServers() {
        Server toBeSavedServer = Server.builder()
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();
        Mockito.when(service.findAll())
                .thenReturn(List.of(toBeSavedServer));

        // When
        List<Server> result = service.findAll();

        // Then
        verify(service, times(1)).findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("server1");
    }

    @Test
    void getServerById_Success(){
        Server server = Server.builder()
                .id(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();
        Mockito.when(service.findById(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"))).thenReturn(server);

        Server serverGot = service.findById(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"));

        assertThat(serverGot).isEqualTo(server);
    }

    @Test
    void getServerById_Failure(){
        UUID uuid = UUID.randomUUID();

        Mockito.when(service.findById(any(UUID.class))).thenThrow(new NotFoundException());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.findById(uuid);
        });
        assertNotNull(exception);
    }

    @Test
    void deleteServer_Success() {
        UUID uuid = UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae");

        Server server = Server.builder()
                .id(uuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();

        Mockito.when(repository.findByIdOptional(uuid)).thenReturn(Optional.ofNullable(server));
        Mockito.when(repository.deleteById(uuid)).thenReturn(true);

        service.delete(uuid);
        verify(service, times(1)).delete(uuid);
    }

//    @Test
//    void deleteServer_Failure(){
//        UUID uuid = UUID.randomUUID();
//
//        Mockito.when(repository.findByIdOptional(uuid)).thenThrow(new NotFoundException());
//        Mockito.when(repository.deleteById(uuid)).thenThrow(new NotFoundException());
//
//        Exception exception = assertThrows(NotFoundException.class, () -> {
//            service.delete(uuid);
//        });
//        assertNotNull(exception);
//    }

    @Test
    void updateServer_Success(){
        UUID uuid = UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae");

        Server oldServer = Server.builder()
                .id(uuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();

        Server newServer = Server.builder()
                .id(uuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server2").build();

        Mockito.when(repository.findById(uuid)).thenReturn(oldServer);
        Mockito.when(service.update(uuid, newServer)).thenReturn(newServer);

        Server updated = service.update(uuid, newServer);
        assertThat(updated).isEqualTo(newServer);
    }

    @Test
    void updateServer_Failure(){
        UUID uuid = UUID.randomUUID();
        Server newServer = Server.builder()
                .id(uuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server2").build();

        Mockito.when(service.update(any(UUID.class), eq(newServer))).thenThrow(new NotFoundException());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.update(uuid, newServer);
        });
        assertNotNull(exception);
    }
}
