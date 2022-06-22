package com.ionos.project.dto;

import com.ionos.project.model.enums.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequestDto(
        UUID id,
        UUID resourceId,
        UUID userId,
        RequestType type,
        RequestStatus status,
        String properties,
        String message,
        LocalDateTime createdAt
) {
}
