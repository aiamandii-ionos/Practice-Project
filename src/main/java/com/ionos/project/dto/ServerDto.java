package com.ionos.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ionos.project.validator.RamConstraint;

import java.util.UUID;

import javax.validation.constraints.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServerDto(
        UUID id,
        UUID userId,
        String ip,
        String privateKey,

        @NotBlank(message = "Name must not be blank")
        String name,

        @NotNull(message = "cores must not be null") @Min(value = 1, message = "cores number must be positive")
        Integer cores,

        @NotNull(message = "ram must not be null") @RamConstraint
        Integer ram,

        @NotNull(message = "storage must not be null") @Min(value = 1, message = "storage value must be positive")
        Integer storage
) {
}