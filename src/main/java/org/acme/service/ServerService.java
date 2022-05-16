package org.acme.service;

import org.acme.model.Server;
import org.acme.repository.ServerRepository;

import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;

@ApplicationScoped
public class ServerService {
    @Inject
    private ServerRepository repository;

    public List<Server> findAll() {
        return repository.findAll().list();
    }

    public Server findById(UUID uuid) {
        Optional<Server> server = repository.findByIdOptional(uuid);
        if (server.isEmpty())
            throw new NotFoundException();
        return server.get();

    }

    @Transactional
    public Server save(Server server) {
        repository.persist(server);
        return server;
    }

    @Transactional
    public Server update(UUID uuid, Server server) {
        Optional<Server> optionalServer = repository.findByIdOptional(uuid);
        if (optionalServer.isEmpty())
            throw new NotFoundException();
        Server serverToUpdate = optionalServer.get();
        serverToUpdate.setName(server.getName());
        serverToUpdate.setCores(server.getCores());
        serverToUpdate.setRam(server.getRam());
        serverToUpdate.setStorage(server.getStorage());
        repository.persist(serverToUpdate);
        return serverToUpdate;
    }

    @Transactional
    public void delete(UUID uuid) {
        Optional<Server> server = repository.findByIdOptional(uuid);
        if (server.isEmpty())
            throw new NotFoundException();
        repository.deleteById(uuid);
    }
}
