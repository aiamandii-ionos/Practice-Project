package org.acme.exception;

public class BadRequestException extends ApiException{
    protected BadRequestException(ErrorMessage errorMessage, Object... params) {
        super(400, errorMessage.getErrorCode(),errorMessage.getErrorMessage(), params);
    }
}
