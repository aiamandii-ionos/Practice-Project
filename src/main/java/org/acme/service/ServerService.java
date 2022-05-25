package org.acme.service;

import io.quarkus.security.identity.SecurityIdentity;
import org.acme.exception.*;
import org.acme.model.Server;
import org.acme.repository.ServerRepository;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.*;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class ServerService {
    @Inject
    private ServerRepository repository;

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
        Optional<Server> server = repository.findByIdOptional(uuid);
        if (server.isPresent()) {
            if (!securityIdentity.hasRole("admin")) {
                if (!server.get().getUserId().toString().equals(jwt.getSubject()))
                    throw new ForbiddenError(ErrorMessage.FORBIDDEN_ERROR, uuid);
            }
            return server.get();
        } else throw new NotFoundException(ErrorMessage.NOT_FOUND, "server", uuid);
    }

    @Transactional
    public Server save(Server server) {
        logger.info("save server");
        server.setUserId(UUID.fromString(jwt.getSubject()));
        repository.persist(server);
        return server;
    }

    @Transactional
    public Server update(UUID uuid, Server server) {
        logger.info("update server by id");
        Optional<Server> serverToUpdate = repository.findByIdOptional(uuid);
        if (serverToUpdate.isPresent()) {
            if (!securityIdentity.hasRole("admin")) {
                if (!serverToUpdate.get().getUserId().toString().equals(jwt.getSubject()))
                    throw new ForbiddenError(ErrorMessage.FORBIDDEN_ERROR, uuid);
            }
            serverToUpdate.get().setName(server.getName());
            serverToUpdate.get().setUserId(UUID.fromString(jwt.getSubject()));
            serverToUpdate.get().setCores(server.getCores());
            serverToUpdate.get().setRam(server.getRam());
            serverToUpdate.get().setStorage(server.getStorage());
            repository.persist(serverToUpdate.get());
            return serverToUpdate.get();
        } else throw new NotFoundException(ErrorMessage.NOT_FOUND, "server", uuid);
    }

    @Transactional
    public void delete(UUID uuid) {
        logger.info("delete server by id");
        Optional<Server> server = repository.findByIdOptional(uuid);
        if (server.isPresent()) {
            if (!securityIdentity.hasRole("admin")) {
                if (!server.get().getUserId().toString().equals(jwt.getSubject()))
                    throw new ForbiddenError(ErrorMessage.FORBIDDEN_ERROR, uuid);
            }
            repository.deleteById(uuid);
        } else throw new NotFoundException(ErrorMessage.NOT_FOUND, "server", uuid);
    }
}
