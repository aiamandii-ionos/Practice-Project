package com.ionos.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ionos.project.exception.*;
import com.ionos.project.model.*;
import com.ionos.project.model.enums.*;
import com.ionos.project.repository.*;
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
    private Logger logger;

    public List<Request> findAll() {
        logger.info("find all requests");
        return repository.getAll();
    }

    @Transactional
    public Request createRequestForServerCreate(Server server) {
        UUID userId = UUID.fromString(jwt.getSubject());
        try {
            String properties = new ObjectMapper().writeValueAsString(server);
            Request createRequest = new Request(RequestType.CREATE_SERVER, RequestStatus.TO_DO, properties, "", LocalDateTime.now(), userId);
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
            Request createRequest = new Request(RequestType.UPDATE_SERVER, RequestStatus.TO_DO, properties, "", LocalDateTime.now(), userId);
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
        Request createRequest = new Request(RequestType.DELETE_SERVER, RequestStatus.TO_DO, server.toString(), "", LocalDateTime.now(), userId);
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
}
