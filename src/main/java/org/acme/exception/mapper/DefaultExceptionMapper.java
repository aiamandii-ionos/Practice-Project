package org.acme.exception.mapper;

import org.acme.dto.*;
import org.acme.exception.ErrorMessage;
import org.jboss.logging.Logger;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.*;

@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {
    @Inject
    private Logger logger;

    @Override
    public Response toResponse(Throwable throwable) {
        logger.error(throwable.getMessage(),throwable);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(new ExceptionResponseDto(500, List.of(new ExceptionDto(ErrorMessage.INTERNAL_SERVER_ERROR.getErrorCode(), ErrorMessage.INTERNAL_SERVER_ERROR.getErrorMessage())))).build();
    }
}
