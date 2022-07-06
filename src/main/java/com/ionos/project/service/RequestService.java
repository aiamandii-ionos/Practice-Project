package com.ionos.project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ionos.project.dto.*;
import com.ionos.project.exception.*;
import com.ionos.project.model.*;
import com.ionos.project.model.enums.*;
import com.ionos.project.repository.*;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class RequestService {
    @Inject
    private RequestRepository repository;

    @Inject
    private ServerRepository serverRepository;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @Inject
    ServerService serverService;

    @Inject
    private Logger logger;

    public List<Request> findAll() {
        logger.info("find all requests");
        return repository.getAll();
    }

    @Scheduled(every = "1s")
    public void scheduleRequests(){
        Request lastRequest = repository.getLastRequest();
        if(lastRequest == null)
            return;
        lastRequest.setStatus(RequestStatus.IN_PROGRESS);
        updateStatus(lastRequest.getRequestId(), lastRequest);

        try {
            Server server = null;
            if(!lastRequest.getProperties().equals(""))
            {
                server = new ObjectMapper().readValue(lastRequest.getProperties(), Server.class);
            }
            switch (lastRequest.getType()){
                case CREATE_SERVER -> {
                    Server createdServer = serverService.save(server);
                    lastRequest.setServer(createdServer);
                    lastRequest.setStatus(RequestStatus.DONE);
                    lastRequest.setMessage("Your request has been processed. The server has been successfully created!");
                    updateStatus(lastRequest.getRequestId(), lastRequest);
                }
                case UPDATE_SERVER -> {
                    Server updatedServer = serverService.update(server.getId(), server);
                    lastRequest.setServer(updatedServer);
                    lastRequest.setStatus(RequestStatus.DONE);
                    lastRequest.setMessage("Your request has been processed. The server has been successfully updated!");
                    updateStatus(lastRequest.getRequestId(), lastRequest);
                }
                case DELETE_SERVER -> {
                    serverService.delete(lastRequest.getServer().getId());
                    lastRequest.setServer(server);
                    lastRequest.setStatus(RequestStatus.DONE);
                    lastRequest.setMessage("Your request has been processed. The server has been successfully deleted!");
                    updateStatus(lastRequest.getRequestId(), lastRequest);
                }

            }
        }  catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    @Transactional
    public Request createRequestForServerCreate(Server server) {
        UUID userId = UUID.fromString(jwt.getSubject());
        server.setUserId(userId);
        try {
            String properties = new ObjectMapper().writeValueAsString(server);
            Request createRequest = Request.builder().type(RequestType.CREATE_SERVER)
                    .message("").properties(properties).status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).userId(userId).build();
            repository.persist(createRequest);
            return createRequest;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new InternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public Request createRequestForServerUpdate(Server server, UUID serverId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Server serverToUpdate = serverRepository.findByIdOptional(serverId).orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND, "server", serverId));
        if (!securityIdentity.hasRole("admin") && !serverToUpdate.getUserId().toString().equals(jwt.getSubject()))
            throw new NotFoundException(ErrorMessage.NOT_FOUND, "server", serverId);
        try {
            String properties = new ObjectMapper().writeValueAsString(server);
            Request createRequest = Request.builder().type(RequestType.UPDATE_SERVER)
                    .message("").properties(properties).status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).userId(userId).build();
            repository.persist(createRequest);
            return createRequest;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new InternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public Request createRequestForServerDelete(UUID serverId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Server server = serverRepository.findByIdOptional(serverId).orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND, "server", serverId));
        if (!securityIdentity.hasRole("admin") && !server.getUserId().toString().equals(jwt.getSubject()))
            throw new NotFoundException(ErrorMessage.NOT_FOUND, "server", serverId);
        Request createRequest = Request.builder().type(RequestType.DELETE_SERVER)
                .message("").status(RequestStatus.TO_DO).properties("").createdAt(LocalDateTime.now()).userId(userId).build();
        repository.persist(createRequest);
        return createRequest;
    }

    public Request findById(UUID uuid) {
        logger.info("find request by id");
        Request request = repository.findByIdOptional(uuid).orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND, "request", uuid));
        if (!securityIdentity.hasRole("admin") && !request.getUserId().toString().equals(jwt.getSubject()))
            throw new NotFoundException(ErrorMessage.NOT_FOUND, "request", uuid);
        return request;
    }

    @Transactional
    public Request updateStatus(UUID uuid, Request request) {
        logger.info("update server by id");
        Request requestToUpdate = repository.findById(uuid);
        requestToUpdate.setStatus(request.getStatus());
        repository.persist(requestToUpdate);
        return requestToUpdate;
    }
}
