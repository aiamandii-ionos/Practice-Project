package org.acme.exception.mapper;

import org.acme.dto.*;
import org.acme.exception.*;


import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.*;
//fa default mapper pentru throwable, care da internal server error
//modifica response, adauga-i http status
//schimba parametrii in clase not found etc

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {
    @Override
    public Response toResponse(ApiException e) {
        return Response.status(e.getStatusCode()).entity(new ExceptionResponseDto(e.getStatusCode(),List.of(new ExceptionDto(e.getErrorCode(), e.getErrorMessage())))).build();
    }
}
