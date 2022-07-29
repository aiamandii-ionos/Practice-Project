package com.ionos.project.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ionos.project.dto.ServerDto;
import com.ionos.project.mapper.*;
import com.ionos.project.model.Request;
import com.ionos.project.model.Server;
import com.ionos.project.model.enums.RequestType;
import com.ionos.project.service.*;

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

    @Inject
    RequestService requestService;

    @Inject
    RequestMapper requestMapper;

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
    @RolesAllowed("user")
    public Response createServer(@Valid ServerDto serverDto) {
        Request request = requestService.createRequest(RequestType.CREATE_SERVER, mapper.toEntity(serverDto), serverDto.id());
        return Response.status(Response.Status.ACCEPTED).entity(requestMapper.toCreateRequestDto(request)).build();
    }

    @PUT
    @Path("/{serverId}")
    @RolesAllowed({"user", "admin"})
    public Response updateById(@PathParam("serverId") UUID serverId, @Valid ServerDto serverDto) {
        Request request = requestService.createRequest(RequestType.UPDATE_SERVER, mapper.toEntity(serverDto), serverId);
        return Response.status(Response.Status.ACCEPTED).entity(requestMapper.toCreateRequestDto(request)).build();
    }

    @DELETE
    @Path("/{serverId}")
    @RolesAllowed({"user", "admin"})
    public Response deleteById(@PathParam("serverId") UUID serverId) {
        Request request = requestService.createRequest(RequestType.DELETE_SERVER, null, serverId);
        return Response.status(Response.Status.ACCEPTED).entity(requestMapper.toDeleteRequestDto(request)).build();
    }
}
