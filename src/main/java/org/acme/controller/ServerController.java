package org.acme.controller;

import org.acme.model.Server;
import org.acme.service.ServerService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/api/servers")
public class ServerController {
    @Inject
    private ServerService service;

    @POST
    public Response post(Server server) {
        final Server saved = service.save(server);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }
}
