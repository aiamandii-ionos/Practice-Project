package org.acme.exception.mapper;

import org.acme.dto.*;
import org.acme.exception.ErrorMessage;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable throwable) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(new ExceptionResponseDto(500, List.of(new ExceptionDto(ErrorMessage.INTERNAL_SERVER_ERROR.getErrorCode(), ErrorMessage.INTERNAL_SERVER_ERROR.getErrorMessage())))).build();
    }
}
