package com.ionos.project.repository;

import io.quarkus.hibernate.orm.panache.*;
import io.quarkus.security.identity.SecurityIdentity;
import com.ionos.project.model.Server;
import org.eclipse.microprofile.jwt.JsonWebToken;


import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ServerRepository implements PanacheRepositoryBase<Server, UUID> {
    @Inject
    JsonWebToken jwt;

    @Inject
    SecurityIdentity securityIdentity;

    public List<Server> getAll() {
        if (securityIdentity.hasRole("admin"))
            return findAll().stream().toList();
        else {
            return list("user_id", UUID.fromString(jwt.getSubject()));
        }
    }
}
