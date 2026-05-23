package com.ega.bank.service;

import com.ega.bank.dto.response.StatsResponse;
import com.ega.bank.dto.response.TransactionResponse;
import com.ega.bank.entity.Compte;
import com.ega.bank.entity.Transaction;
import com.ega.bank.enums.TypeCompte;
import com.ega.bank.enums.TypeTransaction;
import com.ega.bank.repository.ClientRepository;
import com.ega.bank.repository.CompteRepository;
import com.ega.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final ClientRepository clientRepository;
    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Récupère toutes les statistiques pour le dashboard admin
     */
    public StatsResponse getDashboardStats() {
        LocalDateTime debutJournee = LocalDate.now().atStartOfDay();
        LocalDateTime debutMois = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // KPIs de base
        long totalClients = clientRepository.count();
        long totalComptes = compteRepository.count();
        long totalTransactions = transactionRepository.count();

        // Solde total
        BigDecimal soldeTotal = compteRepository.findAll().stream()
                .map(Compte::getSolde)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Répartition des comptes par type
        long comptesCourtants = compteRepository.countByTypeCompte(TypeCompte.COURANT);
        long comptesEpargne = compteRepository.countByTypeCompte(TypeCompte.EPARGNE);

        // Transactions aujourd'hui
        long transactionsAujourdhui = transactionRepository.countAfterDate(debutJournee);

        // Répartition des transactions par type
        long totalDepots = transactionRepository.countByTypeTransaction(TypeTransaction.DEPOT);
        long totalRetraits = transactionRepository.countByTypeTransaction(TypeTransaction.RETRAIT);
        long totalVirements = transactionRepository.countByTypeTransaction(TypeTransaction.VIREMENT);

        // Montants par type
        List<Transaction> allTransactions = transactionRepository.findAll();

        BigDecimal montantDepots = allTransactions.stream()
                .filter(t -> t.getTypeTransaction() == TypeTransaction.DEPOT)
                .map(Transaction::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montantRetraits = allTransactions.stream()
                .filter(t -> t.getTypeTransaction() == TypeTransaction.RETRAIT)
                .map(Transaction::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montantVirements = allTransactions.stream()
                .filter(t -> t.getTypeTransaction() == TypeTransaction.VIREMENT)
                .map(Transaction::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Évolution mensuelle (12 derniers mois)
        List<StatsResponse.MonthlyStats> evolutionMensuelle = getEvolutionMensuelle();

        // Dernières 10 transactions
        List<TransactionResponse> dernieresTransactions = transactionRepository.findLatestTransactions()
                .stream()
                .limit(10)
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());

        return StatsResponse.builder()
                .totalClients(totalClients)
                .totalComptes(totalComptes)
                .soldeTotal(soldeTotal)
                .totalTransactions(totalTransactions)
                .transactionsAujourdhui(transactionsAujourdhui)
                .comptesCourtants(comptesCourtants)
                .comptesEpargne(comptesEpargne)
                .totalDepots(totalDepots)
                .totalRetraits(totalRetraits)
                .totalVirements(totalVirements)
                .montantDepots(montantDepots)
                .montantRetraits(montantRetraits)
                .montantVirements(montantVirements)
                .evolutionMensuelle(evolutionMensuelle)
                .dernieresTransactions(dernieresTransactions)
                .build();
    }

    /**
     * Calcule l'évolution des transactions sur les 12 derniers mois
     */
    private List<StatsResponse.MonthlyStats> getEvolutionMensuelle() {
        List<StatsResponse.MonthlyStats> stats = new ArrayList<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH);

        for (int i = 11; i >= 0; i--) {
            LocalDate mois = now.minusMonths(i);
            LocalDateTime debutMois = mois.withDayOfMonth(1).atStartOfDay();
            LocalDateTime finMois = mois.withDayOfMonth(mois.lengthOfMonth()).atTime(23, 59, 59);

            List<Transaction> transactionsMois = transactionRepository.findAll().stream()
                    .filter(t -> !t.getDateTransaction().isBefore(debutMois)
                            && !t.getDateTransaction().isAfter(finMois))
                    .collect(Collectors.toList());

            long depots = transactionsMois.stream()
                    .filter(t -> t.getTypeTransaction() == TypeTransaction.DEPOT).count();
            long retraits = transactionsMois.stream()
                    .filter(t -> t.getTypeTransaction() == TypeTransaction.RETRAIT).count();
            long virements = transactionsMois.stream()
                    .filter(t -> t.getTypeTransaction() == TypeTransaction.VIREMENT).count();

            BigDecimal montantTotal = transactionsMois.stream()
                    .map(Transaction::getMontant)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            stats.add(StatsResponse.MonthlyStats.builder()
                    .mois(mois.format(formatter))
                    .nombreTransactions((long) transactionsMois.size())
                    .montantTotal(montantTotal)
                    .depots(depots)
                    .retraits(retraits)
                    .virements(virements)
                    .build());
        }

        return stats;
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .typeTransaction(transaction.getTypeTransaction())
                .montant(transaction.getMontant())
                .dateTransaction(transaction.getDateTransaction())
                .description(transaction.getDescription())
                .numeroCompteSource(
                        transaction.getCompteSource() != null ? transaction.getCompteSource().getNumeroCompte() : null)
                .numeroCompteDestination(transaction.getCompteDestination() != null
                        ? transaction.getCompteDestination().getNumeroCompte()
                        : null)
                .build();
    }
}
