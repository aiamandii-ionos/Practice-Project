package com.ionos.project.service;

import com.ionos.project.exception.*;
import com.ionos.project.model.Server;
import com.ionos.project.repository.ServerRepository;
import com.ionoscloud.ApiException;
import com.ionoscloud.ApiResponse;
import com.ionoscloud.model.Datacenter;
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

        logger.info("create server for Ionos Cloud");
        ApiResponse<com.ionoscloud.model.Server> serverResponse = ionosCloudService.createServer(datacenterApiResponse.getData(), server);
        ionosCloudService.checkRequestStatusIsDone(ionosCloudService.getRequestId(serverResponse.getHeaders()));

        server.setServerIonosId(UUID.fromString(Objects.requireNonNull(serverResponse.getData().getId())));
    }

    @Transactional
    public Server update(UUID uuid, Server server) {
        logger.info("update server by id");
        Server serverToUpdate = repository.findByIdOptional(uuid).orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND, "server", uuid));
        if (!securityIdentity.hasRole("admin") && !serverToUpdate.getUserId().toString().equals(jwt.getSubject()))
            throw new NotFoundException(ErrorMessage.NOT_FOUND, uuid);
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
        repository.deleteById(uuid);
    }
}
