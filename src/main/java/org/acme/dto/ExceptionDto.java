package org.acme.dto;

public record ExceptionDto(
        int errorCode,
        String errorMessage) {
}
