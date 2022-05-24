package org.acme.exception;

public class ForbiddenError extends ApiException {
    public ForbiddenError(ErrorMessage errorMessage, Object... params) {
        super(403, errorMessage.getErrorCode(), errorMessage.getErrorMessage(), params);
    }
}
