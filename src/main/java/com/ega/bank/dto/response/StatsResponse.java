package com.ega.bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsResponse {

    // KPIs généraux
    private Long totalClients;
    private Long totalComptes;
    private BigDecimal soldeTotal;
    private Long totalTransactions;

    // Nouveaux clients ce mois
    private Long nouveauxClientsMois;

    // Transactions aujourd'hui
    private Long transactionsAujourdhui;

    // Répartition des comptes
    private Long comptesCourtants;
    private Long comptesEpargne;

    // Répartition des transactions par type
    private Long totalDepots;
    private Long totalRetraits;
    private Long totalVirements;

    // Montants par type
    private BigDecimal montantDepots;
    private BigDecimal montantRetraits;
    private BigDecimal montantVirements;

    // Évolution mensuelle (12 derniers mois)
    private List<MonthlyStats> evolutionMensuelle;

    // Dernières transactions
    private List<TransactionResponse> dernieresTransactions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyStats {
        private String mois;
        private Long nombreTransactions;
        private BigDecimal montantTotal;
        private Long depots;
        private Long retraits;
        private Long virements;
    }
}
