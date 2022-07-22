package com.ionos.project.service;

import com.ionos.project.model.Server;
import com.ionos.project.repository.*;
import com.ionoscloud.ApiResponse;
import com.ionoscloud.api.IpBlocksApi;
import com.ionoscloud.model.*;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.regex.*;

import javax.ws.rs.NotFoundException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServerServiceTest {
    @InjectMocks
    ServerService serverService;

    @InjectMocks
    RequestService requestService;

    @Mock
    RequestRepository requestRepository;

    @Mock
    ServerRepository repository;

    @Mock
    IonosCloudService ionosCloudService;

    @Mock
    IpBlocksApi ipBlocksApi;

    @Mock
    Logger logger;

    @Mock
    JsonWebToken jwt;

    @Mock
    SecurityIdentity securityIdentity;

    Map<String, List<String>> generateHeaders(){
        Pattern pattern = Pattern.compile("([-\\w]+)=\\[(.*?)]");
        Matcher matcher = pattern.matcher("{access-control-allow-headers=[*], access-control-allow-methods=[*], access-control-allow-origin=[*], access-control-expose-headers=[*], content-length=[671], content-type=[application/json], date=[Mon, 20 Jun 2022 12:54:03 GMT], location=[https://api.ionos.com/cloudapi/v5/requests/6eaa56eb-328b-4c12-a891-b9c177a39fba/status], server=[nginx], x-frame-options=[SAMEORIGIN], x-ratelimit-burst=[50], x-ratelimit-limit=[120], x-ratelimit-remaining=[49]}");

        Map<String, List<String>> map = new HashMap<>();
        while (matcher.find()) {
            String key = matcher.group(1);
            String val = matcher.group(2);

            map.put(key, Arrays.asList(val.split("\\s,\\s")));
        }
        return map;
    }

    @Test
    void saveServer_Success() {
        UUID uuid = UUID.randomUUID();

        UUID datacenterId = UUID.fromString("b8ce4ef6-8bd0-462a-88f8-100d26b9126d");

        Server toBeSavedServer = Server.builder()
                .userId(uuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .dataCenterId(datacenterId)
                .serverIonosId(uuid)
                .ipBlockIonosId(uuid)
                .volumeId(uuid)
                .name("server1").build();

        doNothing().when(repository).persist(toBeSavedServer);

        Map<String, List<String>> map = generateHeaders();

        IpBlock ipBlock = mock(IpBlock.class);
        LanPost lanPost = mock(LanPost.class);
        IpBlockProperties ipBlockProperties = mock(IpBlockProperties.class);
        com.ionoscloud.model.Server server = mock(com.ionoscloud.model.Server.class);
        Volume volume = mock(Volume.class);

        String id = "5efc1b40-d8d5-4e6f-913e-990016685c4a";
        Mockito.when(ipBlock.getId()).thenReturn(id);
        Mockito.when(ipBlock.getProperties()).thenReturn(ipBlockProperties);
        Mockito.when(ipBlockProperties.getIps()).thenReturn(List.of("1.2.3.4"));
        Mockito.when(server.getId()).thenReturn(id);
        Mockito.when(volume.getId()).thenReturn(id);


        ApiResponse<LanPost> apiResponse = new ApiResponse<>(202, map, lanPost);
        ApiResponse<IpBlock> apiResponseIpBlock = new ApiResponse<>(202, map, ipBlock);
        ApiResponse<Nic> nicApiResponse = new ApiResponse<>(202, map);
        ApiResponse<com.ionoscloud.model.Server> serverApiResponse = new ApiResponse<>(202, map, server);
        ApiResponse<Volume> volumeApiResponse = new ApiResponse<>(202, map, volume);

        Mockito.when(ionosCloudService.createLan(String.valueOf(datacenterId))).thenReturn(apiResponse);
        Mockito.when(ionosCloudService.createIpBlock()).thenReturn(apiResponseIpBlock);
        Mockito.when(ionosCloudService.createNic(ipBlock, lanPost, datacenterId.toString(), server.getId())).thenReturn(nicApiResponse);
        Mockito.when(ionosCloudService.createServer(String.valueOf(datacenterId), toBeSavedServer)).thenReturn(serverApiResponse);

        String datacenter = datacenterId.toString();
        String serv = server.getId();
        Integer toBeSaved = toBeSavedServer.getStorage();
        Mockito.when(ionosCloudService.attachVolume(eq(datacenter), eq(serv), anyString(), eq(toBeSaved))).thenReturn(volumeApiResponse);


        Server result = serverService.save(toBeSavedServer, uuid);
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

        Exception exception = assertThrows(RuntimeException.class, () -> {
            serverService.save(toBeSavedServer, uuid);
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

        Mockito.when(repository.getAll())
                .thenReturn(List.of(toBeSavedServer));

        List<Server> result = serverService.findAll();

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

        Server serverGot = serverService.findById(UUID.fromString("a848a45e-d065-11ec-a62f-2d718d2fcfae"));

        assertThat(serverGot).isEqualTo(server);
    }

    @Test
    void getServerById_Failure() {
        UUID uuid = UUID.randomUUID();

        Mockito.when(repository.findByIdOptional(any(UUID.class))).thenThrow(new NotFoundException());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            serverService.findById(uuid);
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
            serverService.findById(uuid);
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
                .dataCenterId(uuid)
                .serverIonosId(uuid)
                .ipBlockIonosId(uuid)
                .volumeId(uuid)
                .name("server1").build();

        Map<String, List<String>> map = generateHeaders();

        ApiResponse<Object> apiResponse = new ApiResponse<>(202, map);

        Mockito.when(repository.findByIdOptional(uuid)).thenReturn(Optional.ofNullable(server));
        Mockito.when(repository.deleteById(uuid)).thenReturn(true);
        Mockito.when(ionosCloudService.deleteServer(server.getDataCenterId().toString(), server.getServerIonosId().toString())).thenReturn(apiResponse);

        serverService.delete(uuid);
        verify(repository, times(1)).deleteById(uuid);
    }

    @Test
    void deleteServer_Failure() {
        UUID uuid = UUID.randomUUID();

        Mockito.when(repository.findByIdOptional(uuid)).thenThrow(new NotFoundException());

        Exception exception = assertThrows(NotFoundException.class, () -> {
            serverService.delete(uuid);
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
                .dataCenterId(uuid)
                .serverIonosId(uuid)

                .name("server1").build();

        Server newServer = Server.builder()
                .id(uuid)
                .userId(userUuid)
                .cores(2)
                .ram(200)
                .storage(30)
                .dataCenterId(uuid)
                .serverIonosId(uuid)
                .name("server2").build();

        Map<String, List<String>> map = generateHeaders();

        ApiResponse<com.ionoscloud.model.Server> apiResponse = new ApiResponse<>(202, map);

        Mockito.when(repository.findByIdOptional(uuid)).thenReturn(Optional.ofNullable(oldServer));
        doNothing().when(repository).persist(any(Server.class));
        Mockito.when(ionosCloudService.updateServer(oldServer.getDataCenterId().toString(),oldServer.getServerIonosId().toString(), newServer)).thenReturn(apiResponse);

        Server updated = serverService.update(uuid, newServer);
        assertEquals(updated.getName(), newServer.getName());
        assertEquals(updated.getCores(), newServer.getCores());
        assertEquals(updated.getStorage(), newServer.getStorage());
        assertEquals(updated.getRam(), newServer.getRam());
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
            serverService.update(uuid, newServer);
        });
        assertNotNull(exception);
    }
}
