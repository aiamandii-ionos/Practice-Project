package com.ionos.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ionos.project.model.enums.*;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.*;

@Entity
public class Request {
    @Id
    private UUID id;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Enumerated(EnumType.STRING)
    private RequestType type;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Column()
    private String properties;

    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "user_id")
    private UUID userId;


}
