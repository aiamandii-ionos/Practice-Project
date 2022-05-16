package org.acme.exception;

public class NotFoundException extends ApiException {
    public NotFoundException(int statusCode, String errorMessage, Object... params) {
        super(404, errorMessage, params);
    }
}
