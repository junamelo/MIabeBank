package com.ega.bank.entity;

import com.ega.bank.enums.StatutDemande;
import com.ega.bank.enums.TypeTransaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "demandes_operations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le type d'opération est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTransaction typeOperation;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateTraitement;

    private String commentaireAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = StatutDemande.EN_ATTENTE;
        }
    }
}
