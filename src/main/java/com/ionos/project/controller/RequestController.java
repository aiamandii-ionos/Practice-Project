package com.ionos.project.controller;

import com.ionos.project.dto.*;
import com.ionos.project.mapper.RequestMapper;
import com.ionos.project.model.*;
import com.ionos.project.service.RequestService;

import java.util.*;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/api/requests")
@RolesAllowed({"user","admin"})
public class RequestController {
    @Inject
    RequestService requestService;

    @Inject
    RequestMapper requestMapper;

    @GET
    public Response get() {
        List<RequestDto> requestDtoList = requestService.findAll().stream().map(request -> requestMapper.toDTO(request)).toList();
        return Response.ok(requestDtoList).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") UUID id) {
        Request request = requestService.findById(id);
        return Response.ok(requestMapper.toDTO(request)).build();
    }
}
