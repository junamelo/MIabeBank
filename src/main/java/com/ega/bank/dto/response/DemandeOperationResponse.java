package com.ega.bank.dto.response;

import com.ega.bank.enums.StatutDemande;
import com.ega.bank.enums.TypeCompte;
import com.ega.bank.enums.TypeTransaction;
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
public class DemandeOperationResponse {

    private Long id;
    private TypeTransaction typeOperation;
    private BigDecimal montant;
    private String description;
    private StatutDemande statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateTraitement;
    private String commentaireAdmin;

    // Informations sur le compte
    private Long compteId;
    private String numeroCompte;
    private TypeCompte typeCompte;

    // Informations sur le client
    private Long clientId;
    private String clientNom;
    private String clientPrenom;

    // Transaction associée (si approuvée)
    private Long transactionId;
}
