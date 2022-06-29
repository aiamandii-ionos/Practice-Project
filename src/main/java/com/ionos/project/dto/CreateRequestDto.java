package com.ionos.project.dto;

import com.fasterxml.jackson.annotation.*;
import com.ionos.project.model.enums.RequestType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateRequestDto(
        UUID requestId,
        @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
                property = "id")
        @JsonIdentityReference(alwaysAsId = true)
        ServerDto server,
        String properties
) {
}
