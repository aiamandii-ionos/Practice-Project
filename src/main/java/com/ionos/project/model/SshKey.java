package com.ionos.project.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class SshKey {
    private String publicKey;
    private String privateKey;
}
