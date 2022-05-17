package org.acme.exception;

public class InternalServerError extends ApiException{
    protected InternalServerError(ErrorMessage errorMessage, Object... params) {
        super(500, errorMessage.getErrorCode(), errorMessage.getErrorMessage(), params);
    }
}
