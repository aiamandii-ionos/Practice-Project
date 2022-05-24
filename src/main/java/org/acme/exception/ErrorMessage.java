package org.acme.exception;

import lombok.*;

@AllArgsConstructor
@Getter
public enum ErrorMessage {
    NOT_FOUND(1, "{0} with id={1} not found!"),
    INTERNAL_SERVER_ERROR(2, "The server encountered an internal error and was unable to complete your request."),

    FORBIDDEN_ERROR(3, "Access forbidden! You are not allowed to access the server with id={0} !");


    private final int errorCode;
    private final String errorMessage;
}
