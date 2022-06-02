package com.ionos.project.model;

import lombok.*;
import org.hibernate.annotations.*;

import java.util.UUID;

import javax.persistence.*;
import javax.persistence.Entity;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Server {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "name")
    private String name;

    @Column(name = "cores")
    private Integer cores;

    @Column(name = "ram")
    private Integer ram;

    @Column(name = "storage")
    private Integer storage;
}
