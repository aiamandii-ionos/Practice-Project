package com.ionos.project.exception;

public class InternalServerError extends ApiException{
    public InternalServerError(ErrorMessage errorMessage, Object... params) {
        super(500, errorMessage.getErrorCode(), errorMessage.getErrorMessage(), params);
    }
}
