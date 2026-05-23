package com.ega.bank.service;

import com.ega.bank.dto.response.TransactionResponse;
import com.ega.bank.entity.Compte;
import com.ega.bank.entity.Transaction;
import com.ega.bank.enums.TypeTransaction;
import com.ega.bank.exception.BadRequestException;
import com.ega.bank.exception.ResourceNotFoundException;
import com.ega.bank.repository.CompteRepository;
import com.ega.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CompteRepository compteRepository;

    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        return mapToResponse(transaction);
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getTransactionsByCompteId(Long compteId) {
        if (!compteRepository.existsById(compteId)) {
            throw new ResourceNotFoundException("Compte", "id", compteId);
        }
        return transactionRepository.findAllByCompteId(compteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getTransactionsByCompteAndPeriode(
            Long compteId, LocalDate dateDebut, LocalDate dateFin) {

        if (!compteRepository.existsById(compteId)) {
            throw new ResourceNotFoundException("Compte", "id", compteId);
        }

        if (dateDebut.isAfter(dateFin)) {
            throw new BadRequestException("La date de début doit être antérieure à la date de fin");
        }

        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(LocalTime.MAX);

        return transactionRepository.findByCompteAndPeriode(compteId, debut, fin).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getTransactionsByClientId(Long clientId) {
        return transactionRepository.findAllByClientId(clientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Annule une transaction et inverse les mouvements de fonds
     */
    @Transactional
    public TransactionResponse annulerTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        if (Boolean.TRUE.equals(transaction.getAnnulee())) {
            throw new BadRequestException("Cette transaction a déjà été annulée");
        }

        // Inverser les mouvements selon le type de transaction
        switch (transaction.getTypeTransaction()) {
            case DEPOT:
                // Annuler un dépôt = retirer du compte destination
                Compte compteDepot = transaction.getCompteDestination();
                if (compteDepot.getSolde().compareTo(transaction.getMontant()) < 0) {
                    throw new BadRequestException("Solde insuffisant pour annuler cette transaction");
                }
                compteDepot.setSolde(compteDepot.getSolde().subtract(transaction.getMontant()));
                compteRepository.save(compteDepot);
                break;

            case RETRAIT:
                // Annuler un retrait = remettre sur le compte source
                Compte compteRetrait = transaction.getCompteSource();
                compteRetrait.setSolde(compteRetrait.getSolde().add(transaction.getMontant()));
                compteRepository.save(compteRetrait);
                break;

            case VIREMENT:
                // Annuler un virement = remettre sur source et retirer de destination
                Compte source = transaction.getCompteSource();
                Compte destination = transaction.getCompteDestination();

                if (destination.getSolde().compareTo(transaction.getMontant()) < 0) {
                    throw new BadRequestException(
                            "Solde insuffisant sur le compte destination pour annuler ce virement");
                }

                source.setSolde(source.getSolde().add(transaction.getMontant()));
                destination.setSolde(destination.getSolde().subtract(transaction.getMontant()));
                compteRepository.save(source);
                compteRepository.save(destination);
                break;
        }

        // Marquer la transaction comme annulée
        transaction.setAnnulee(true);
        transaction.setDateAnnulation(LocalDateTime.now());
        transaction.setDescription(transaction.getDescription() + " [ANNULÉE]");

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .typeTransaction(transaction.getTypeTransaction())
                .montant(transaction.getMontant())
                .dateTransaction(transaction.getDateTransaction())
                .description(transaction.getDescription())
                .numeroCompteSource(transaction.getCompteSource() != null
                        ? transaction.getCompteSource().getNumeroCompte()
                        : null)
                .numeroCompteDestination(transaction.getCompteDestination() != null
                        ? transaction.getCompteDestination().getNumeroCompte()
                        : null)
                .annulee(transaction.getAnnulee())
                .dateAnnulation(transaction.getDateAnnulation())
                .build();
    }
}
