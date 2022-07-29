package com.ionos.project.exception;

public class RequestNotCreatedException extends ApiException{
    public RequestNotCreatedException(ErrorMessage errorMessage, Object... params) {
        super(405, errorMessage.getErrorCode(), errorMessage.getErrorMessage(), params);
    }
}
