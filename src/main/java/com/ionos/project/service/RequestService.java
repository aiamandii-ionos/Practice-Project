package com.ionos.project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ionos.project.exception.*;
import com.ionos.project.model.*;
import com.ionos.project.model.enums.*;
import com.ionos.project.repository.*;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.*;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
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

    public List<Request> findAll(Integer page, Integer size, String type, String status, String start, String end) {
        logger.info("find all requests");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime dateStart = null, dateEnd = null;
        if (start!=null)
            dateStart = LocalDateTime.parse(start, formatter);
        if(end!=null)
            dateEnd = LocalDateTime.parse(end, formatter);
        Parameters params = Parameters.with("userId", UUID.fromString(jwt.getSubject()))
                .and("type", RequestType.of(type)).and("status", RequestStatus.of(status)).and("dateStart", dateStart).and("dateEnd", dateEnd);
        PanacheQuery<Request> query = repository.getAll(params);
        return query.page(Page.of(page, size)).list();
    }

    @Transactional
    public Request createRequest(RequestType requestType, Server server, UUID serverId) {

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
                    List<Request> requestList = repository.findRequestsByServerId(serverId);
                    for (Request r : requestList) {
                        if (r.getType() == RequestType.DELETE_SERVER) {
                            if (r.getStatus() == RequestStatus.TO_DO || r.getStatus() == RequestStatus.IN_PROGRESS)
                                throw new RequestNotCreatedException(ErrorMessage.OPERATION_NOT_ALLOWED);
                        }
                    }
                    request = Request.builder().type(RequestType.UPDATE_SERVER).message("").properties(properties).status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).server(serverToUpdate).userId(userId).build();
                    repository.persist(request);
                }
                case DELETE_SERVER -> {
                    logger.info("create request for server delete");
                    Server serverToDelete = checkIfServerExistsInDatabase(serverId);
                    List<Request> requestList = repository.findRequestsByServerId(serverId);
                    for (Request r : requestList) {
                        if (r.getType() == RequestType.UPDATE_SERVER || r.getType() == RequestType.DELETE_SERVER) {
                            if (r.getStatus() == RequestStatus.TO_DO || r.getStatus() == RequestStatus.IN_PROGRESS)
                                throw new RequestNotCreatedException(ErrorMessage.OPERATION_NOT_ALLOWED);
                        }
                    }
                    request = Request.builder().type(RequestType.DELETE_SERVER).message("").properties("{}").status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).server(serverToDelete).userId(userId).build();
                    repository.persist(request);
                }

            }
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            throw new InternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
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
        logger.info("getting server from request properties");
        Server server = null;
        String request = "";
        try {
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
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            throw new InternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
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
