package org.acme.dto;

import org.acme.validator.RamConstraint;

import java.util.UUID;

import javax.validation.constraints.*;

public record ServerDto(
        UUID id,
        @NotBlank
        String name,

        @NotNull @Min(value = 1, message = "number must be positive")
        Integer cores,

        @NotNull @RamConstraint
        Integer ram,

        @NotNull @Min(value = 1, message = "number must be positive")
        Integer storage
) {
}