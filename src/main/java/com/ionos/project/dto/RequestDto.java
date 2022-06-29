package com.ionos.project.dto;

import com.fasterxml.jackson.annotation.*;
import com.ionos.project.model.enums.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequestDto(
        UUID requestId,
        @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
                property = "id")
        @JsonIdentityReference(alwaysAsId = true)
        ServerDto server,
        UUID userId,
        RequestType type,
        RequestStatus status,
        String properties,
        String message,
        LocalDateTime createdAt
) {
}
