package com.ionos.project.service;

import com.ionos.project.exception.*;
import com.ionos.project.exception.ErrorMessage;
import com.ionos.project.model.Server;
import com.ionos.project.repository.ServerRepository;
import com.ionoscloud.ApiException;
import com.ionoscloud.ApiResponse;
import com.ionoscloud.model.*;
import com.jcraft.jsch.*;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.io.*;
import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;


//change keycloak to use roles allowed in code
//modify tests
//scenario outline
@ApplicationScoped
public class ServerService {
    @Inject
    private ServerRepository repository;

    @Inject
    private IonosCloudService ionosCloudService;

    @Inject
    private Logger logger;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    public List<Server> findAll() {
        logger.info("find all servers");
        return repository.getAll();
    }

    public Server findById(UUID uuid) {
        logger.info("find server by id");
        Server server = repository.findByIdOptional(uuid).orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND, "server", uuid));
        if (!securityIdentity.hasRole("admin") && !server.getUserId().toString().equals(jwt.getSubject()))
            throw new NotFoundException(ErrorMessage.NOT_FOUND, "server", uuid);
        return server;
    }

    @Transactional
    public Server save(Server server) {
        logger.info("save server");
        server.setUserId(UUID.fromString(jwt.getSubject()));
        saveIonosServer(server);
        repository.persist(server);
        return server;
    }

    public void saveIonosServer(Server server) {
        logger.info("Set datacenter id for server");
        UUID datacenterId = ConfigProvider.getConfig().getValue("datacenterId", UUID.class);
        server.setDataCenterId(datacenterId);

        logger.info("create lan for Ionos Cloud");
        ApiResponse<LanPost> lanPostApiResponse = ionosCloudService.createLan(String.valueOf(datacenterId));
        ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(lanPostApiResponse.getHeaders()));

        logger.info("create ip block for Ionos Cloud");
        ApiResponse<IpBlock> ipBlockApiResponse = ionosCloudService.createIpBlock();
        ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(ipBlockApiResponse.getHeaders()));
        UUID ipBlockId = UUID.fromString(Objects.requireNonNull(ipBlockApiResponse.getData().getId()));
        server.setIpBlockIonosId(ipBlockId);
        server.setIp(ipBlockApiResponse.getData().getProperties().getIps().get(0));


        logger.info("create server for Ionos Cloud");
        ApiResponse<com.ionoscloud.model.Server> serverResponse = ionosCloudService.createServer(String.valueOf(datacenterId), server);
        ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(serverResponse.getHeaders()));
        server.setServerIonosId(UUID.fromString(Objects.requireNonNull(serverResponse.getData().getId())));

        logger.info("generate ssh key for server");
        String privateKey;
        String publicKey;
        try (ByteArrayOutputStream pubKeyOS = new ByteArrayOutputStream()) {
            try (ByteArrayOutputStream prvKeyOS = new ByteArrayOutputStream()) {
                JSch jsch = new JSch();
                KeyPair keypair = KeyPair.genKeyPair(jsch, KeyPair.RSA, 2048);
                keypair.writePrivateKey(prvKeyOS);
                keypair.writePublicKey(pubKeyOS, "key");
                publicKey = pubKeyOS.toString().substring(0, pubKeyOS.toString().length()-1);
                privateKey = prvKeyOS.toString();
                keypair.dispose();

            } catch (IOException | JSchException e) {
                logger.error(e.getStackTrace());
                throw new InternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            logger.error(e.getStackTrace());
            throw new InternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
        server.setPrivateKey(Base64.getEncoder().encodeToString(privateKey.getBytes()));

        logger.info("create and attach volume to server for Ionos Cloud");
        ApiResponse<Volume> attachVolumeResponse = ionosCloudService.attachVolume(String.valueOf(datacenterId), String.valueOf(server.getServerIonosId()), publicKey, server.getStorage());
        ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(attachVolumeResponse.getHeaders()));
        server.setVolumeId(UUID.fromString(Objects.requireNonNull(attachVolumeResponse.getData().getId())));

        logger.info("create nic for Ionos Cloud");
        ApiResponse<Nic> nicApiResponse = ionosCloudService.createNic(ipBlockApiResponse.getData(), lanPostApiResponse.getData(), String.valueOf(datacenterId), serverResponse.getData().getId());
        ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(nicApiResponse.getHeaders()));
    }

    public void deleteIonosServer(Server server) {
        logger.info("delete datacenter and server for Ionos Cloud");
        ApiResponse<Object> deleteServerApiResponse = ionosCloudService.deleteServer(server.getDataCenterId().toString(), server.getServerIonosId().toString());
        ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(deleteServerApiResponse.getHeaders()));

        logger.info("delete ip blocks for Ionos Cloud");
        ionosCloudService.deleteIpBlock(String.valueOf(server.getIpBlockIonosId()));

        logger.info("delete volume for Ionos Cloud");
        ionosCloudService.deleteVolume(server.getDataCenterId().toString(), server.getVolumeId().toString());
    }

    public void updateIonosServer(Server serverToUpdate, Server newServer) {
        logger.info("update server for Ionos Cloud");
        ApiResponse<com.ionoscloud.model.Server> updateServerApiResponse = ionosCloudService.updateServer
                (serverToUpdate.getDataCenterId().toString(), serverToUpdate.getServerIonosId().toString(), newServer);
        ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(updateServerApiResponse.getHeaders()));
    }

    @Transactional
    public Server update(UUID uuid, Server server) {
        logger.info("update server by id");
        Server serverToUpdate = repository.findByIdOptional(uuid).orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND, "server", uuid));
        if (!securityIdentity.hasRole("admin") && !serverToUpdate.getUserId().toString().equals(jwt.getSubject()))
            throw new NotFoundException(ErrorMessage.NOT_FOUND, "server", uuid);
        updateIonosServer(serverToUpdate, server);
        serverToUpdate.setName(server.getName());
        serverToUpdate.setUserId(UUID.fromString(jwt.getSubject()));
        serverToUpdate.setCores(server.getCores());
        serverToUpdate.setRam(server.getRam());
        serverToUpdate.setStorage(server.getStorage());
        repository.persist(serverToUpdate);
        return serverToUpdate;
    }

    @Transactional
    public void delete(UUID uuid) {
        logger.info("delete server by id");
        Server server = repository.findByIdOptional(uuid).orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND, "server", uuid));
        if (!securityIdentity.hasRole("admin") && !server.getUserId().toString().equals(jwt.getSubject()))
            throw new NotFoundException(ErrorMessage.NOT_FOUND, "server", uuid);
        deleteIonosServer(server);
        repository.deleteById(uuid);
    }
}
