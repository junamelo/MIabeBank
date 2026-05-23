package com.ega.bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private Boolean enabled;
    private LocalDateTime createdAt;

    // Informations du client lié (si existant)
    private Long clientId;
    private String clientNom;
    private String clientPrenom;
    private String clientEmail;
    private String clientTelephone;
}
