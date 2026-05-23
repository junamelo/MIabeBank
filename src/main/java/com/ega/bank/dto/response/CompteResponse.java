package com.ega.bank.dto.response;

import com.ega.bank.enums.TypeCompte;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteResponse {

    private Long id;
    private String numeroCompte;
    private TypeCompte typeCompte;
    private LocalDateTime dateCreation;
    private BigDecimal solde;
    private Long clientId;
    private String clientNom;
    private String clientPrenom;
}
