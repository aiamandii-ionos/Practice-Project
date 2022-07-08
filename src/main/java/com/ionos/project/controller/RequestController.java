package com.ionos.project.controller;

import com.ionos.project.dto.*;
import com.ionos.project.mapper.RequestMapper;
import com.ionos.project.model.*;
import com.ionos.project.service.RequestService;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.*;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/api/requests")
@RolesAllowed({"user", "admin"})
public class RequestController {
    @Inject
    RequestService requestService;

    @Inject
    RequestMapper requestMapper;

    @Inject
    JsonWebToken jwt;

    @GET
    public Response getAll() {
        List<GetAllRequestsDto> requestDtoList = requestService.findAll(UUID.fromString(jwt.getSubject())).stream().map(request -> requestMapper.toGetAllRequestsDto(request)).toList();
        return Response.ok(requestDtoList).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        Request request = requestService.findById(id);
        return Response.ok(requestMapper.toDTO(request)).build();
    }
}
