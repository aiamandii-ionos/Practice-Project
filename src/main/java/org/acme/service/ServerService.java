package org.acme.service;

import org.acme.model.Server;
import org.acme.repository.ServerRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class ServerService {
    @Inject
    private ServerRepository repository;

    @Transactional
    public Server save(Server server) {
        repository.persist(server);
        return server;
    }
}
