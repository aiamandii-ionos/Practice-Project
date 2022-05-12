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
    public Response createServer(Server server) {
        final Server saved = service.save(server);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    @PUT
    @Path("/{serverId}")
    public Response updateById(@PathParam("serverId") UUID serverId, Server server){
        Server saved = service.update(serverId,server);
        return Response.ok(saved).build();
    }

    @DELETE
    @Path("/{serverId}")
    public Response deleteById(@PathParam("serverId") UUID serverId){
        return service.delete(serverId);
    }
}
