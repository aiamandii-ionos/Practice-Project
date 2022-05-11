package org.acme.repository;

import io.quarkus.hibernate.orm.panache.*;
import org.acme.model.Server;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServerRepository implements PanacheRepositoryBase<Server, UUID> {
}
