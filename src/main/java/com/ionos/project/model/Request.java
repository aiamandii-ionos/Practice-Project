package com.ionos.project.model;

import com.ionos.project.model.enums.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NamedQueries({
        @NamedQuery(name = "Request.getAllAdmin", query = "from Request where (:type is null or type = :type) and (:status is null or status = :status) and (cast(cast(:dateStart as text) as timestamp) is null or created_at >= cast(cast(:dateStart as text) as timestamp)) and (cast(cast(:dateEnd as text) as timestamp) is null or created_at < cast(cast(:dateEnd as text) as timestamp)) order by created_at desc"),
        @NamedQuery(name = "Request.getAllUser", query = "from Request where user_id=:userId and (:type is null or type = :type) and (:status is null or status = :status) and (cast(cast(:dateStart as text) as timestamp) is null or created_at >= cast(cast(:dateStart as text) as timestamp)) and (cast(cast(:dateEnd as text) as timestamp) is null or created_at < cast(cast(:dateEnd as text) as timestamp)) order by created_at desc")
})
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
    private RequestType type;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Column(name = "properties")
    private String properties;

    @Column(name = "message")
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Server server;
}
