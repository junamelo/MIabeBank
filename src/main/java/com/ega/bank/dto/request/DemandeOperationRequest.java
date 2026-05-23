package com.ega.bank.dto.request;

import com.ega.bank.enums.TypeTransaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeOperationRequest {

    @NotNull(message = "Le type d'opération est obligatoire")
    private TypeTransaction typeOperation;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal montant;

    @NotNull(message = "L'ID du compte est obligatoire")
    private Long compteId;

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères")
    private String description;
}
