package org.acme.exception;

public class NotFoundException extends ApiException {
    public NotFoundException(ErrorMessage errorMessage, Object... params) {
        super(404, errorMessage.getErrorCode(),errorMessage.getErrorMessage(),params);
    }
}
