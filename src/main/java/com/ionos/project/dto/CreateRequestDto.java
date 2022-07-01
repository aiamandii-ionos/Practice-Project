package com.ionos.project.dto;

import com.fasterxml.jackson.annotation.*;
import org.jose4j.json.internal.json_simple.JSONObject;

import java.util.UUID;

public record CreateRequestDto(
        UUID requestId,
        @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
                property = "id")
        @JsonIdentityReference(alwaysAsId = true)
        ServerDto server,
        JSONObject properties
) {
}
