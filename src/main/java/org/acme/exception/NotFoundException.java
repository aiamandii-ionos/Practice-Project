package org.acme.exception;

public class NotFoundException extends ApiException {
    //in loc sa dai error message ca param, fa un enum cu error message + status code
    public NotFoundException(ErrorMessage errorMessage, Object... params) {
        super(404, errorMessage.getErrorCode(),errorMessage.getErrorMessage(),params);
    }
}
