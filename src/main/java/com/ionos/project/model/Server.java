package com.ionos.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.*;

import java.util.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
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

    @Column(name = "datacenter_id")
    @ToString.Exclude
    private UUID dataCenterId;

    @Column(name = "server_ionos_id")
    @ToString.Exclude
    private UUID serverIonosId;

    @Column(name = "ipblock_ionos_id")
    @ToString.Exclude
    private UUID ipBlockIonosId;

    @Column(name = "volume_id")
    @ToString.Exclude
    private UUID volumeId;

    @Column(name = "ip")
    @ToString.Exclude
    private String ip;

    @Column(name = "name")
    private String name;

    @Column(name = "cores")
    private Integer cores;

    @Column(name = "ram")
    private Integer ram;

    @Column(name = "storage")
    private Integer storage;

    @Column(length = 2048, name = "private_key")
    @ToString.Exclude
    private String privateKey;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    @ToString.Exclude
    private List<Request> requestList;
}
