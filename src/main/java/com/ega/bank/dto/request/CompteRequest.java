package com.ega.bank.dto.request;

import com.ega.bank.enums.TypeCompte;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteRequest {

    // Optionnel pour /mon-compte (déterminé par l'utilisateur connecté)
    // Obligatoire pour /comptes (création par admin)
    private Long clientId;

    @NotNull(message = "Le type de compte est obligatoire")
    private TypeCompte typeCompte;

    @PositiveOrZero(message = "Le solde initial doit être positif ou nul")
    @Builder.Default
    private BigDecimal soldeInitial = BigDecimal.ZERO;
}
