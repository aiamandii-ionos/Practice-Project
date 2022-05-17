package org.acme.exception.mapper;

import org.acme.dto.*;

import java.util.ArrayList;

import javax.validation.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.*;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException e) {
        ExceptionResponseDto exceptionResponseDto = new ExceptionResponseDto(400, new ArrayList<>());
        int errorCode = 4;
        for(ConstraintViolation<?> constraint: e.getConstraintViolations()){
            exceptionResponseDto.errorMessages().add(new ExceptionDto(errorCode++,constraint.getMessage()));
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(exceptionResponseDto).build();
    }
}
