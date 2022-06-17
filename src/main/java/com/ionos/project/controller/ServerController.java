package com.ionos.project.controller;

import com.ionos.project.dto.ServerDto;
import com.ionos.project.mapper.ServerMapper;
import com.ionos.project.model.Server;
import com.ionos.project.service.*;
import com.ionoscloud.ApiResponse;
import com.ionoscloud.model.Datacenter;

import java.util.*;

import javax.annotation.security.RolesAllowed;
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
    @RolesAllowed({"user", "admin"})
    public Response get() {
        List<ServerDto> serverDtoList = service.findAll().stream().map(server -> mapper.toDTO(server)).toList();
        return Response.ok(serverDtoList).build();
    }

    @GET
    @Path("/{serverId}")
    @RolesAllowed({"user", "admin"})
    public Response getById(@PathParam("serverId") UUID serverId) {
        Server server = service.findById(serverId);
        return Response.ok(mapper.toDTO(server)).build();
    }

    @POST
    @Path("/create")
    @RolesAllowed("user")
    public Response createServer(@Valid ServerDto serverDto) {
        final Server saved = service.save(mapper.toEntity(serverDto));
        return Response.status(Response.Status.CREATED).entity(mapper.toDTO(saved)).build();
    }

    @PUT
    @Path("/{serverId}")
    @RolesAllowed({"user", "admin"})
    public Response updateById(@PathParam("serverId") UUID serverId, @Valid ServerDto serverDto) {
        Server saved = service.update(serverId, mapper.toEntity(serverDto));
        return Response.ok(mapper.toDTO(saved)).build();
    }

    @DELETE
    @Path("/{serverId}")
    @RolesAllowed({"user", "admin"})
    public Response deleteById(@PathParam("serverId") UUID serverId) {
        service.delete(serverId);
        return Response.status(204).build();
    }
}
