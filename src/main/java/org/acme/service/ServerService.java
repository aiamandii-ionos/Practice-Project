package org.acme.service;

import org.acme.model.Server;
import org.acme.repository.ServerRepository;

import java.util.*;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;

@ApplicationScoped
public class ServerService {
    @Inject
    private ServerRepository repository;

    public List<Server> findAll(){
        return repository.findAll().list();
    }

    public Server findById(UUID uuid){
        Optional<Server> server = repository.findByIdOptional(uuid);
        if(server.isPresent())
            return server.get();
        else throw new NotFoundException();

    }

    @Transactional
    public Server save(Server server) {
        repository.persist(server);
        return server;
    }
}
