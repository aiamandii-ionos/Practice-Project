package org.acme.exception;

import lombok.*;

import java.text.MessageFormat;

@Getter
@Setter
public abstract class ApiException extends RuntimeException{
    private final int statusCode;
    private final String errorMessage;


    protected ApiException(int statusCode, String errorMessage, Object... params) {
        this.statusCode = statusCode;
        this.errorMessage = formatMessage(errorMessage, params);
    }

    private static String formatMessage(String message, Object... messageArgs) {
        return MessageFormat.format(message, messageArgs);
    }
}
