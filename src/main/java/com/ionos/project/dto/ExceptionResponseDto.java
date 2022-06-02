package com.ionos.project.dto;

import java.util.List;

public record ExceptionResponseDto(int statusCode, List<ExceptionDto> errorMessages) {
}
