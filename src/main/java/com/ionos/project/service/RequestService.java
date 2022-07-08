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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

//refactoring
//mai pune loguri
//face switch pt create
// scoate user id din properties
//fa o singura metoda de update
// adauga for cu request in metoda
// sorteaza get all dupa data
// List<Request> requestList = findAll(lastRequest.getUserId()); in loc de asta gaseste dupa server
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

    public List<Request> findAll(UUID userId) {
        logger.info("find all requests");
        return repository.getAll(userId);
    }

    @Scheduled(every = "1s")
    public void scheduleRequests(){
        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();
        Request lastRequest = repository.getLastRequest();
        if(lastRequest == null)
            return;
        lastRequest.setStatus(RequestStatus.IN_PROGRESS);
        updateStatusRequest(lastRequest.getRequestId(), lastRequest);

        reentrantLock.unlock();

        try {
            Server server = null;
            if(!lastRequest.getProperties().isEmpty())
            {
                server = new ObjectMapper().readValue(lastRequest.getProperties(), Server.class);
            }
            switch (lastRequest.getType()){
                case CREATE_SERVER -> {
                    Server createdServer = serverService.save(server);
                    lastRequest.setServer(createdServer);
                    lastRequest.setStatus(RequestStatus.DONE);
                    lastRequest.setMessage("Your request has been processed. The server has been successfully created!");
                    updateCreateRequest(lastRequest.getRequestId(), lastRequest);
                }
                case UPDATE_SERVER -> {
                    Server updatedServer = serverService.update(lastRequest.getServer().getId(), server);
                    lastRequest.setServer(updatedServer);
                    lastRequest.setStatus(RequestStatus.DONE);
                    lastRequest.setMessage("Your request has been processed. The server has been successfully updated!");
                    updateCreateRequest(lastRequest.getRequestId(), lastRequest);
                }
                case DELETE_SERVER -> {
                    String uuid = String.valueOf(lastRequest.getServer().getId());
                    List<Request> requestList = findAll(lastRequest.getUserId());
                    for(Request r: requestList)
                    {
                        if(r.getServer()!=null && Objects.equals(String.valueOf(r.getServer().getId()), uuid))
                        {
                            r.setServer(null);
                            updateCreateRequest(r.getRequestId(), r);
                        }
                    }

                    serverService.delete(UUID.fromString(uuid));
                    lastRequest.setStatus(RequestStatus.DONE);
                    lastRequest.setMessage("Your request has been processed. The server has been successfully deleted!");
                    updateStatusRequest(lastRequest.getRequestId(), lastRequest);
                }

            }
        } catch (ApiException e){
            lastRequest.setStatus(RequestStatus.FAILED);
            lastRequest.setMessage(e.getErrorMessage());
            updateStatusRequest(lastRequest.getRequestId(), lastRequest);
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            lastRequest.setStatus(RequestStatus.FAILED);
            lastRequest.setMessage(ErrorMessage.INTERNAL_SERVER_ERROR.getErrorMessage());
            updateStatusRequest(lastRequest.getRequestId(), lastRequest);
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
                    .message("").properties(properties).status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).server(serverToUpdate).userId(userId).build();
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
        try {
            Request createRequest = Request.builder().type(RequestType.DELETE_SERVER)
                    .message("").properties("{}").status(RequestStatus.TO_DO).createdAt(LocalDateTime.now()).server(server).userId(userId).build();
            repository.persist(createRequest);
            return createRequest;
        } catch (Exception e) {
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
    public Request updateCreateRequest(UUID uuid, Request request) {
        logger.info("update server by id");
        Request requestToUpdate = repository.findById(uuid);
        requestToUpdate.setStatus(request.getStatus());
        requestToUpdate.setMessage(request.getMessage());
        if(request.getServer() != null)
            requestToUpdate.setServer(request.getServer());
        else
            requestToUpdate.setServer(null);
        repository.persist(requestToUpdate);
        return requestToUpdate;
    }

    @Transactional
    public Request updateStatusRequest(UUID uuid, Request request){
        logger.info("update server by id");
        Request requestToUpdate = repository.findById(uuid);
        requestToUpdate.setStatus(request.getStatus());
        requestToUpdate.setMessage(request.getMessage());
        repository.persist(requestToUpdate);
        return requestToUpdate;
    }


}
