package org.acme.controller;

import org.acme.dto.ServerDto;
import org.acme.mapper.ServerMapper;
import org.acme.model.Server;
import org.acme.service.ServerService;

import java.util.*;

import javax.annotation.security.*;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/api/servers")
public class ServerController {
    @Inject
    private ServerService service;

    @Inject
    private ServerMapper mapper;

    @GET
    public Response get() {
        List<ServerDto> serverDtoList = service.findAll().stream().map(server -> mapper.toDTO(server)).toList();
        return Response.ok(serverDtoList).build();
    }

    @GET
    @Path("/{serverId}")
    public Response getById(@PathParam("serverId") UUID serverId) {
        Server server = service.findById(serverId);
        return Response.ok(mapper.toDTO(server)).build();
    }

    @POST
    @Path("/create")
    public Response createServer(@Valid ServerDto serverDto) {
        final Server saved = service.save(mapper.toEntity(serverDto));
        return Response.status(Response.Status.CREATED).entity(mapper.toDTO(saved)).build();
    }

    @PUT
    @Path("/{serverId}")
    public Response updateById(@PathParam("serverId") UUID serverId, @Valid ServerDto serverDto) {
        Server saved = service.update(serverId, mapper.toEntity(serverDto));
        return Response.ok(mapper.toDTO(saved)).build();
    }

    @DELETE
    @Path("/{serverId}")
    public Response deleteById(@PathParam("serverId") UUID serverId) {
        service.delete(serverId);
        return Response.status(204).build();
    }
}
