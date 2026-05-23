package com.ega.bank.entity;

import com.ega.bank.enums.TypeTransaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le type de transaction est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTransaction typeTransaction;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private LocalDateTime dateTransaction;

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean annulee = false;

    private LocalDateTime dateAnnulation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_source_id")
    private Compte compteSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_destination_id")
    private Compte compteDestination;

    @PrePersist
    protected void onCreate() {
        this.dateTransaction = LocalDateTime.now();
    }
}
