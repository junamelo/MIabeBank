package com.ega.bank.dto.response;

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
public class TransactionResponse {

    private Long id;
    private TypeTransaction typeTransaction;
    private BigDecimal montant;
    private LocalDateTime dateTransaction;
    private String description;
    private String numeroCompteSource;
    private String numeroCompteDestination;
    private Boolean annulee;
    private LocalDateTime dateAnnulation;
}
