package com.ionos.project.model;

import com.ionos.project.model.enums.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.*;

@Entity
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "request_id")
    private UUID requestId;

    @Enumerated(EnumType.STRING)
    @NonNull
    private RequestType type;

    @Enumerated(EnumType.STRING)
    @NonNull
    private RequestStatus status;

    @Column(name = "properties")
    @NonNull
    private String properties;

    @Column(name = "message")
    @NonNull
    private String message;

    @Column(name = "created_at")
    @NonNull
    private LocalDateTime createdAt;

    @Column(name = "user_id")
    @NonNull
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Server server;
}
