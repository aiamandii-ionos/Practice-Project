package org.acme.dto;

import org.acme.validator.RamConstraint;

import java.util.UUID;

import javax.validation.constraints.*;

public record ServerDto(
        UUID id,
        @NotBlank(message = "Name must not be blank")
        String name,

        @NotNull @Min(value = 1, message = "cores number must be positive")
        Integer cores,

        @NotNull(message = "ram must not be null") @RamConstraint
        Integer ram,

        @NotNull(message = "storage must not be null") @Min(value = 1, message = "storage value must be positive")
        Integer storage
) {
}