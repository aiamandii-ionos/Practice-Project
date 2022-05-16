package org.acme.exception.mapper;

import org.acme.exception.*;


import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ApiExceptionMapper implements ExceptionMapper<ApiException> {
    @Override
    public Response toResponse(ApiException e) {
        return Response.status(e.getStatusCode()).build();
    }
}
