package com.ionos.project.dto;

import com.fasterxml.jackson.annotation.*;
import com.ionos.project.model.enums.*;
import org.jose4j.json.internal.json_simple.JSONObject;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RequestDto(
        UUID requestId,
        @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
                property = "id")
        @JsonIdentityReference(alwaysAsId = true)
        ServerDto server,
        UUID userId,
        RequestType type,
        RequestStatus status,
        JSONObject properties,
        String message,
        LocalDateTime createdAt
) {
}
