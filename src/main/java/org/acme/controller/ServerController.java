package org.acme.controller;

import org.acme.model.Server;
import org.acme.service.ServerService;

import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/api/servers")
public class ServerController {
    @Inject
    private ServerService service;

    @GET
    public Response get(){
        return Response.ok(service.findAll()).build();
    }

    @GET
    @Path("/{serverId}")
    public Response getById(@PathParam("serverId") UUID serverId){
        return Response.ok(service.findById(serverId)).build();
    }

    @POST
    public Response post(Server server) {
        final Server saved = service.save(server);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }
}
