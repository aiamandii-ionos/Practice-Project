package com.ionos.project.repository;

import com.ionos.project.model.*;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.*;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class RequestRepository implements PanacheRepositoryBase<Request, UUID> {
    @Inject
    JsonWebToken jwt;

    @Inject
    SecurityIdentity securityIdentity;

    public List<Request> getAll(){
        if(securityIdentity.hasRole("admin"))
            return findAll().stream().toList();
        else {
            return list("user_id", UUID.fromString(jwt.getSubject()));
        }
    }

    public List<Request> findRequestsByServerId(UUID serverId) {
        return find("server_id", serverId).stream().collect(Collectors.toList());
    }
}
