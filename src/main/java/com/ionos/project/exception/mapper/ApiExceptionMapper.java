package com.ionos.project.exception.mapper;

import com.ionos.project.dto.*;
import com.ionos.project.exception.ApiException;


import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.*;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {
    @Override
    public Response toResponse(ApiException e) {
        return Response.status(e.getStatusCode()).entity(new ExceptionResponseDto(e.getStatusCode(),List.of(new ExceptionDto(e.getErrorCode(), e.getErrorMessage())))).build();
    }
}
