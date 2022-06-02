package com.ionos.project.exception.mapper;

import com.ionos.project.dto.*;

import java.util.ArrayList;

import javax.validation.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.*;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException e) {
        ExceptionResponseDto exceptionResponseDto = new ExceptionResponseDto(422, new ArrayList<>());
        for(ConstraintViolation<?> constraint: e.getConstraintViolations()){
            exceptionResponseDto.errorMessages().add(new ExceptionDto(-1,constraint.getMessage()));
        }
        return Response.status(422).entity(exceptionResponseDto).build();
    }
}
