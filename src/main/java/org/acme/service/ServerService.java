package org.acme.service;

import org.acme.exception.*;
import org.acme.model.Server;
import org.acme.repository.ServerRepository;
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
    private Logger logger;

    public List<Server> findAll() {
        logger.info("find all servers");
        return repository.findAll().list();
    }

    public Server findById(UUID uuid) {
        logger.info("find server by id");
        Optional<Server> server = repository.findByIdOptional(uuid);
        if (server.isEmpty())
            throw new NotFoundException(ErrorMessage.NOT_FOUND, "server", uuid);
        return server.get();

    }

    @Transactional
    public Server save(Server server) {
        logger.info("save server");
        repository.persist(server);
        return server;
    }

    @Transactional
    public Server update(UUID uuid, Server server) {
        logger.info("update server by id");
        Server serverToUpdate = repository.findByIdOptional(uuid).orElseThrow(() ->
                new NotFoundException(ErrorMessage.NOT_FOUND, "server", uuid));
        serverToUpdate.setName(server.getName());
        serverToUpdate.setUserId(server.getUserId());
        serverToUpdate.setCores(server.getCores());
        serverToUpdate.setRam(server.getRam());
        serverToUpdate.setStorage(server.getStorage());
        repository.persist(serverToUpdate);
        return serverToUpdate;
    }

    @Transactional
    public void delete(UUID uuid) {
        logger.info("delete server by id");
        Optional<Server> server = repository.findByIdOptional(uuid);
        if (server.isEmpty())
            throw new NotFoundException(ErrorMessage.NOT_FOUND, "server", uuid);
        repository.deleteById(uuid);
    }
}
