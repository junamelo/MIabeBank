package com.ega.bank.entity;

import com.ega.bank.enums.TypeCompte;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comptes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le numéro de compte est obligatoire")
    @Column(nullable = false, unique = true)
    private String numeroCompte;

    @NotNull(message = "Le type de compte est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCompte typeCompte;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @NotNull(message = "Le solde est obligatoire")
    @DecimalMin(value = "0.0", message = "Le solde ne peut pas être négatif")
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal solde = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client proprietaire;

    @OneToMany(mappedBy = "compteSource", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Transaction> transactionsEmises = new ArrayList<>();

    @OneToMany(mappedBy = "compteDestination", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Transaction> transactionsRecues = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        if (this.solde == null) {
            this.solde = BigDecimal.ZERO;
        }
    }
}
