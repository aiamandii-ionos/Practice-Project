package com.ionos.project.service;

import com.ionos.project.exception.*;
import com.ionos.project.exception.ErrorMessage;
import com.ionos.project.model.Server;
import com.ionos.project.repository.ServerRepository;
import com.ionoscloud.ApiException;
import com.ionoscloud.ApiResponse;
import com.ionoscloud.model.*;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

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
//        System.out.println(ionosCloudService.findAllDatacenters());
//        System.out.println(ionosCloudService.findAllIpBlocks());
        return repository.getAll();
    }

    public Server findById(UUID uuid) {
        logger.info("find server by id");
        Server server = repository.findByIdOptional(uuid).orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND, "server", uuid));
        if (!securityIdentity.hasRole("admin") && !server.getUserId().toString().equals(jwt.getSubject()))
            throw new NotFoundException(ErrorMessage.NOT_FOUND, uuid);
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
        logger.info("create datacenter for Ionos Cloud");
        ApiResponse<Datacenter> datacenterApiResponse = ionosCloudService.createDatacenter();
        ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(datacenterApiResponse.getHeaders()));
        UUID datacenterId = UUID.fromString(Objects.requireNonNull(datacenterApiResponse.getData().getId()));
        server.setDataCenterId(datacenterId);

        logger.info("create lan for Ionos Cloud");
        ApiResponse<LanPost> lanPostApiResponse = ionosCloudService.createLan(datacenterApiResponse.getData().getId());
        ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(lanPostApiResponse.getHeaders()));

        logger.info("create ip block for Ionos Cloud");
        ApiResponse<IpBlock> ipBlockApiResponse = ionosCloudService.createIpBlock();
        ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(ipBlockApiResponse.getHeaders()));
        UUID ipBlockId = UUID.fromString(Objects.requireNonNull(ipBlockApiResponse.getData().getId()));
        server.setIpBlockIonosId(ipBlockId);
        server.setIp(ipBlockApiResponse.getData().getProperties().getIps().get(0));

        logger.info("create volume for Ionos Cloud");
        //ApiResponse<Volume> volumeApiResponse = ionosCloudService.createVolume(Objects.requireNonNull(datacenterApiResponse.getData().getId()), server.getStorage());
        //ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(volumeApiResponse.getHeaders()));

        logger.info("create server for Ionos Cloud");
        ApiResponse<com.ionoscloud.model.Server> serverResponse = ionosCloudService.createServer(datacenterApiResponse.getData().getId(), server);
        ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(serverResponse.getHeaders()));
        server.setServerIonosId(UUID.fromString(Objects.requireNonNull(serverResponse.getData().getId())));

        ApiResponse<Nic> nicApiResponse = ionosCloudService.createNic(ipBlockApiResponse.getData(), lanPostApiResponse.getData(), datacenterApiResponse.getData().getId(), serverResponse.getData().getId());
        ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(nicApiResponse.getHeaders()));
    }

    public void deleteIonosServer(Server server){
        logger.info("delete datacenter and server for Ionos Cloud");
        ApiResponse<Object> deleteDatacenterApiResponse = ionosCloudService.deleteDatacenter(server.getDataCenterId().toString());
        ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(deleteDatacenterApiResponse.getHeaders()));

        logger.info("delete ip blocks for Ionos Cloud");
        ionosCloudService.deleteIpBlock(String.valueOf(server.getIpBlockIonosId()));
    }

    public void updateIonosServer(Server serverToUpdate, Server newServer){
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
            throw new NotFoundException(ErrorMessage.NOT_FOUND, uuid);
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
            throw new NotFoundException(ErrorMessage.NOT_FOUND, uuid);
        deleteIonosServer(server);
        repository.deleteById(uuid);
    }
}
