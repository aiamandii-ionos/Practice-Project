package org.acme.dto;

import java.util.List;

public record ExceptionResponseDto(int statusCode, List<ExceptionDto> errorMessages) {
}
