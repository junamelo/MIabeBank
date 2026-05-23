package com.ega.bank.service;

import com.ega.bank.dto.request.DemandeOperationRequest;
import com.ega.bank.dto.response.DemandeOperationResponse;
import com.ega.bank.entity.Client;
import com.ega.bank.entity.Compte;
import com.ega.bank.entity.DemandeOperation;
import com.ega.bank.entity.Transaction;
import com.ega.bank.enums.StatutDemande;
import com.ega.bank.enums.TypeTransaction;
import com.ega.bank.exception.BadRequestException;
import com.ega.bank.exception.ResourceNotFoundException;
import com.ega.bank.repository.ClientRepository;
import com.ega.bank.repository.CompteRepository;
import com.ega.bank.repository.DemandeOperationRepository;
import com.ega.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DemandeOperationService {

    private final DemandeOperationRepository demandeRepository;
    private final CompteRepository compteRepository;
    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Créer une nouvelle demande d'opération (dépôt ou retrait)
     */
    @Transactional
    public DemandeOperationResponse creerDemande(DemandeOperationRequest request, Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

        Compte compte = compteRepository.findById(request.getCompteId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte", "id", request.getCompteId()));

        // Vérifier que le compte appartient au client
        if (!compte.getProprietaire().getId().equals(clientId)) {
            throw new BadRequestException("Ce compte ne vous appartient pas");
        }

        // Vérifier le type d'opération
        if (request.getTypeOperation() != TypeTransaction.DEPOT &&
                request.getTypeOperation() != TypeTransaction.RETRAIT) {
            throw new BadRequestException("Type d'opération invalide. Seuls DEPOT et RETRAIT sont autorisés.");
        }

        // Pour un retrait, vérifier que le solde est suffisant
        if (request.getTypeOperation() == TypeTransaction.RETRAIT) {
            if (compte.getSolde().compareTo(request.getMontant()) < 0) {
                throw new BadRequestException("Solde insuffisant pour cette demande de retrait");
            }
        }

        DemandeOperation demande = DemandeOperation.builder()
                .typeOperation(request.getTypeOperation())
                .montant(request.getMontant())
                .description(request.getDescription())
                .compte(compte)
                .client(client)
                .statut(StatutDemande.EN_ATTENTE)
                .build();

        demande = demandeRepository.save(demande);
        log.info("Nouvelle demande de {} créée pour le client {} - Montant: {}",
                request.getTypeOperation(), client.getPrenom() + " " + client.getNom(), request.getMontant());

        return mapToResponse(demande);
    }

    /**
     * Récupérer toutes les demandes en attente (pour l'admin)
     */
    public List<DemandeOperationResponse> getDemandesEnAttente() {
        return demandeRepository.findByStatutOrderByDateCreationAsc(StatutDemande.EN_ATTENTE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer toutes les demandes (pour l'admin)
     */
    public List<DemandeOperationResponse> getAllDemandes() {
        return demandeRepository.findAllByOrderByDateCreationDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les demandes d'un client
     */
    public List<DemandeOperationResponse> getDemandesByClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

        return demandeRepository.findByClientOrderByDateCreationDesc(client)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Compter les demandes en attente
     */
    public long countDemandesEnAttente() {
        return demandeRepository.countByStatut(StatutDemande.EN_ATTENTE);
    }

    /**
     * Approuver une demande (admin uniquement) - exécute l'opération
     */
    @Transactional
    public DemandeOperationResponse approuverDemande(Long demandeId, String commentaire) {
        DemandeOperation demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande", "id", demandeId));

        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new BadRequestException("Cette demande a déjà été traitée");
        }

        Compte compte = demande.getCompte();
        BigDecimal montant = demande.getMontant();

        // Exécuter l'opération selon le type
        Transaction transaction;
        if (demande.getTypeOperation() == TypeTransaction.DEPOT) {
            // Effectuer le dépôt
            compte.setSolde(compte.getSolde().add(montant));
            compteRepository.save(compte);

            transaction = Transaction.builder()
                    .typeTransaction(TypeTransaction.DEPOT)
                    .montant(montant)
                    .description(
                            "Dépôt approuvé - " + (demande.getDescription() != null ? demande.getDescription() : ""))
                    .compteDestination(compte)
                    .dateTransaction(LocalDateTime.now())
                    .build();
        } else {
            // Effectuer le retrait
            if (compte.getSolde().compareTo(montant) < 0) {
                throw new BadRequestException("Solde insuffisant pour effectuer ce retrait");
            }
            compte.setSolde(compte.getSolde().subtract(montant));
            compteRepository.save(compte);

            transaction = Transaction.builder()
                    .typeTransaction(TypeTransaction.RETRAIT)
                    .montant(montant)
                    .description(
                            "Retrait approuvé - " + (demande.getDescription() != null ? demande.getDescription() : ""))
                    .compteSource(compte)
                    .dateTransaction(LocalDateTime.now())
                    .build();
        }

        transaction = transactionRepository.save(transaction);

        // Mettre à jour la demande
        demande.setStatut(StatutDemande.APPROUVEE);
        demande.setDateTraitement(LocalDateTime.now());
        demande.setCommentaireAdmin(commentaire);
        demande.setTransaction(transaction);
        demande = demandeRepository.save(demande);

        log.info("Demande {} approuvée - {} de {} sur le compte {}",
                demandeId, demande.getTypeOperation(), montant, compte.getNumeroCompte());

        return mapToResponse(demande);
    }

    /**
     * Rejeter une demande (admin uniquement)
     */
    @Transactional
    public DemandeOperationResponse rejeterDemande(Long demandeId, String motif) {
        DemandeOperation demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande", "id", demandeId));

        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new BadRequestException("Cette demande a déjà été traitée");
        }

        demande.setStatut(StatutDemande.REJETEE);
        demande.setDateTraitement(LocalDateTime.now());
        demande.setCommentaireAdmin(motif);
        demande = demandeRepository.save(demande);

        log.info("Demande {} rejetée - Motif: {}", demandeId, motif);

        return mapToResponse(demande);
    }

    /**
     * Récupérer une demande par ID
     */
    public DemandeOperationResponse getDemandeById(Long id) {
        DemandeOperation demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande", "id", id));
        return mapToResponse(demande);
    }

    private DemandeOperationResponse mapToResponse(DemandeOperation demande) {
        return DemandeOperationResponse.builder()
                .id(demande.getId())
                .typeOperation(demande.getTypeOperation())
                .montant(demande.getMontant())
                .description(demande.getDescription())
                .statut(demande.getStatut())
                .dateCreation(demande.getDateCreation())
                .dateTraitement(demande.getDateTraitement())
                .commentaireAdmin(demande.getCommentaireAdmin())
                .compteId(demande.getCompte().getId())
                .numeroCompte(demande.getCompte().getNumeroCompte())
                .typeCompte(demande.getCompte().getTypeCompte())
                .clientId(demande.getClient().getId())
                .clientNom(demande.getClient().getNom())
                .clientPrenom(demande.getClient().getPrenom())
                .transactionId(demande.getTransaction() != null ? demande.getTransaction().getId() : null)
                .build();
    }
}
