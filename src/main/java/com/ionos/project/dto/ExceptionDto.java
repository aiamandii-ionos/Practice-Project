package com.ionos.project.dto;

public record ExceptionDto(
        int errorCode,
        String errorMessage) {
}
