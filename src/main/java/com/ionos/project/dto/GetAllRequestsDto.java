package com.ionos.project.dto;

import com.ionos.project.model.enums.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetAllRequestsDto(
        UUID requestId,
        RequestType type,
        LocalDateTime createdAt
) {
}
