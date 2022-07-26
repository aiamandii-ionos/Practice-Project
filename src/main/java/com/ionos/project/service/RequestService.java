package com.ionos.project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ionos.project.exception.*;
import com.ionos.project.model.*;
import com.ionos.project.model.enums.*;
import com.ionos.project.repository.*;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.time.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

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

    private final ReentrantLock reentrantLock = new ReentrantLock();

    public List<Request> findAll() {
        logger.info("find all requests");
        return repository.getAll();
    }

    @Transactional
    public Request createRequest(RequestType requestType, Server server, UUID serverId) throws JsonProcessingException {

        Request request = null;
        try {
            UUID userId = UUID.fromString(jwt.getSubject());
            String properties = new ObjectMapper().writeValueAsString(server);
            switch (requestType) {
                case CREATE_SERVER -> {
                    logger.info("create request for server create");
                    request = Request.builder().type(RequestType.CREATE_SERVER).message("").properties(properties).status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).userId(userId).build();
                    repository.persist(request);
                }
                case UPDATE_SERVER -> {
                    logger.info("create request for server update");
                    Server serverToUpdate = checkIfServerExistsInDatabase(serverId);
                    request = Request.builder().type(RequestType.UPDATE_SERVER).message("").properties(properties).status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).server(serverToUpdate).userId(userId).build();
                    repository.persist(request);
                }
                case DELETE_SERVER -> {
                    logger.info("create request for server delete");
                    Server serverToDelete = checkIfServerExistsInDatabase(serverId);
                    request = Request.builder().type(RequestType.DELETE_SERVER).message("").properties("{}").status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).server(serverToDelete).userId(userId).build();
                    repository.persist(request);
                }

            }
        } finally {

        }
        return request;
    }

    private Server checkIfServerExistsInDatabase(UUID serverId) {
        Server server = serverRepository.findByIdOptional(serverId).orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND, "server", serverId));
        if (!securityIdentity.hasRole("admin") && !server.getUserId().toString().equals(jwt.getSubject()))
            throw new NotFoundException(ErrorMessage.NOT_FOUND, "server", serverId);
        return server;
    }

    @Scheduled(every = "1s")
    public void scheduleRequests() {
        logger.info("getting last request in scheduler");
        reentrantLock.lock();
        Request lastRequest = repository.getLastRequest();

        if (lastRequest == null) return;
        lastRequest.setStatus(RequestStatus.IN_PROGRESS);
        updateStatusRequest(lastRequest.getRequestId(), lastRequest);

        reentrantLock.unlock();

        try {
            logger.info("getting server from request properties");
            Server server = null;
            String request = "";
            if (!lastRequest.getProperties().isEmpty()) {
                server = new ObjectMapper().readValue(lastRequest.getProperties(), Server.class);
            }
            switch (lastRequest.getType()) {
                case CREATE_SERVER -> {
                    logger.info("create server and update request status");
                    Server createdServer = serverService.create(server, lastRequest.getUserId());
                    request = "created";
                    lastRequest.setServer(createdServer);
                }
                case UPDATE_SERVER -> {
                    logger.info("update server and update request status");
                    Server updatedServer = serverService.update(lastRequest.getServer().getId(), server);
                    request = "updated";
                    lastRequest.setServer(updatedServer);
                }
                case DELETE_SERVER -> {
                    logger.info("delete server and update request status");
                    UUID uuid = lastRequest.getServer().getId();
                    request = "deleted";

                    updateRequestServerAfterDelete(uuid);

                    serverService.delete(uuid);
                }
            }
            lastRequest.setStatus(RequestStatus.DONE);
            lastRequest.setMessage("Your request has been processed. The server has been successfully " + request);
            updateStatusRequest(lastRequest.getRequestId(), lastRequest);
        } catch (ApiException e) {
            logger.error(e.getErrorMessage(), e);
            lastRequest.setStatus(RequestStatus.FAILED);
            lastRequest.setMessage(e.getErrorMessage());
            updateStatusRequest(lastRequest.getRequestId(), lastRequest);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            lastRequest.setStatus(RequestStatus.FAILED);
            lastRequest.setMessage(ErrorMessage.INTERNAL_SERVER_ERROR.getErrorMessage());
            updateStatusRequest(lastRequest.getRequestId(), lastRequest);
        }

    }

    public Request findById(UUID uuid) {
        logger.info("find request by id");
        Request request = repository.findByIdOptional(uuid).orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND, "request", uuid));
        if (!securityIdentity.hasRole("admin") && !request.getUserId().toString().equals(jwt.getSubject()))
            throw new NotFoundException(ErrorMessage.NOT_FOUND, "request", uuid);
        return request;
    }

    @Transactional
    public void updateStatusRequest(UUID uuid, Request request) {
        logger.info("update request status");
        Request requestToUpdate = repository.findById(uuid);
        requestToUpdate.setStatus(request.getStatus());
        requestToUpdate.setMessage(request.getMessage());
        if (request.getServer() != null) requestToUpdate.setServer(request.getServer());
        else requestToUpdate.setServer(null);
        repository.persist(requestToUpdate);
    }

    public void updateRequestServerAfterDelete(UUID serverId) {
        List<Request> requestList = repository.findRequestsByServerId(serverId);
        for (Request r : requestList) {
            r.setServer(null);
            updateStatusRequest(r.getRequestId(), r);
        }
    }
}
