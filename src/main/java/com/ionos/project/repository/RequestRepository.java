package com.ionos.project.repository;

import com.ionos.project.model.*;
import com.ionos.project.model.enums.*;
import io.quarkus.hibernate.orm.panache.*;
import io.quarkus.panache.common.Parameters;
import io.quarkus.security.identity.SecurityIdentity;

import java.util.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class RequestRepository implements PanacheRepositoryBase<Request, UUID> {
    @Inject
    SecurityIdentity securityIdentity;

    public PanacheQuery<Request> getAll(Parameters params) {
        if (securityIdentity.hasRole("admin"))
            return find("#Request.getAllAdmin", params);
        else {
            return find("#Request.getAllUser", params);
        }
    }

    public Request getLastRequest() {
        return find("status = ?1 order by created_at", RequestStatus.TO_DO).stream().findFirst().orElse(null);
    }

    public List<Request> findRequestsByServerId(UUID serverId) {
        return list("resource_id", serverId);
    }
}
