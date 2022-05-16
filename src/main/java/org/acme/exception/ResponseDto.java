package org.acme.exception;

import java.util.List;

public record ResponseDto(List<ExceptionDto> errorMessages) {
}
