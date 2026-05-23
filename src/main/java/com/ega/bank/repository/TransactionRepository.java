package com.ega.bank.repository;

import com.ega.bank.entity.Transaction;
import com.ega.bank.enums.TypeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

        List<Transaction> findByCompteSourceId(Long compteId);

        List<Transaction> findByCompteDestinationId(Long compteId);

        List<Transaction> findByTypeTransaction(TypeTransaction typeTransaction);

        // Transactions d'un compte (source ou destination) dans une période
        @Query("SELECT t FROM Transaction t WHERE " +
                        "(t.compteSource.id = :compteId OR t.compteDestination.id = :compteId) " +
                        "AND t.dateTransaction BETWEEN :dateDebut AND :dateFin " +
                        "ORDER BY t.dateTransaction DESC")
        List<Transaction> findByCompteAndPeriode(
                        @Param("compteId") Long compteId,
                        @Param("dateDebut") LocalDateTime dateDebut,
                        @Param("dateFin") LocalDateTime dateFin);

        // Toutes les transactions d'un compte
        @Query("SELECT t FROM Transaction t WHERE " +
                        "t.compteSource.id = :compteId OR t.compteDestination.id = :compteId " +
                        "ORDER BY t.dateTransaction DESC")
        List<Transaction> findAllByCompteId(@Param("compteId") Long compteId);

        // Transactions d'un client (tous ses comptes)
        @Query("SELECT t FROM Transaction t WHERE " +
                        "t.compteSource.proprietaire.id = :clientId OR t.compteDestination.proprietaire.id = :clientId "
                        +
                        "ORDER BY t.dateTransaction DESC")
        List<Transaction> findAllByClientId(@Param("clientId") Long clientId);

        // Compter par type de transaction
        long countByTypeTransaction(TypeTransaction typeTransaction);

        // Transactions après une date
        @Query("SELECT t FROM Transaction t WHERE t.dateTransaction >= :date ORDER BY t.dateTransaction DESC")
        List<Transaction> findAllAfterDate(@Param("date") LocalDateTime date);

        // Compter transactions après une date
        @Query("SELECT COUNT(t) FROM Transaction t WHERE t.dateTransaction >= :date")
        long countAfterDate(@Param("date") LocalDateTime date);

        // Compter par type après une date
        @Query("SELECT COUNT(t) FROM Transaction t WHERE t.typeTransaction = :type AND t.dateTransaction >= :date")
        long countByTypeAfterDate(@Param("type") TypeTransaction type, @Param("date") LocalDateTime date);

        // Dernières N transactions
        @Query("SELECT t FROM Transaction t ORDER BY t.dateTransaction DESC")
        List<Transaction> findLatestTransactions();
}
