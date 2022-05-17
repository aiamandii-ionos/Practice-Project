package org.acme.exception;

import lombok.*;

import java.text.MessageFormat;

@Getter
@Setter
public abstract class ApiException extends RuntimeException {
    private final int statusCode;
    private final int errorCode;
    private final String errorMessage;


    protected ApiException(int statusCode, int errorCode, String errorMessage, Object... params) {
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.errorMessage = formatMessage(errorMessage, params);
    }

    private static String formatMessage(String message, Object... messageArgs) {
        return MessageFormat.format(message, messageArgs);
    }
}
