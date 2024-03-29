package com.ionos.project.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import org.hibernate.annotations.*;

import java.util.*;

import javax.persistence.*;
import javax.persistence.Entity;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private UUID dataCenterId;

    @Column(name = "server_ionos_id")
    private UUID serverIonosId;

    @Column(name = "ipblock_ionos_id")
    private UUID ipBlockIonosId;

    @Column(name = "volume_id")
    private UUID volumeId;

    @Column(name = "ip")
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
    private String privateKey;

    @OneToMany(mappedBy = "server", orphanRemoval = false)
    @JsonIgnore
    private List<Request> requestList;
}
