package org.acme.exception;

import lombok.*;

@AllArgsConstructor
@Getter
public enum ErrorMessage {
    NOT_FOUND(1, "{0} with id={1} not found!"),
    INTERNAL_SERVER_ERROR(2, "The server encountered an internal error and was unable to complete your request.");


    private final int errorCode;
    private final String errorMessage;
}
