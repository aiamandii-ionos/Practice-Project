package com.ionos.project.exception;

import javax.ws.rs.core.Response;

public class RequestNotCreatedException extends ApiException{
    public RequestNotCreatedException(ErrorMessage errorMessage, Object... params) {
        super(Response.Status.CONFLICT.getStatusCode(), errorMessage.getErrorCode(), errorMessage.getErrorMessage(), params);
    }
}
