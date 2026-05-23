package com.ega.bank.service;

import com.ega.bank.dto.request.CompteRequest;
import com.ega.bank.dto.request.DepotRetraitRequest;
import com.ega.bank.dto.request.VirementRequest;
import com.ega.bank.dto.response.CompteResponse;
import com.ega.bank.entity.Client;
import com.ega.bank.entity.Compte;
import com.ega.bank.entity.Transaction;
import com.ega.bank.enums.TypeTransaction;
import com.ega.bank.exception.BadRequestException;
import com.ega.bank.exception.ResourceNotFoundException;
import com.ega.bank.exception.SoldeInsuffisantException;
import com.ega.bank.entity.User;
import com.ega.bank.repository.ClientRepository;
import com.ega.bank.repository.CompteRepository;
import com.ega.bank.repository.TransactionRepository;
import com.ega.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompteService {

    private final CompteRepository compteRepository;
    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public CompteResponse creerCompte(CompteRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.getClientId()));

        // Générer un numéro IBAN unique
        String numeroCompte = genererNumeroCompte();

        // Utiliser le solde initial ou 0 par défaut
        BigDecimal soldeInitial = request.getSoldeInitial() != null ? request.getSoldeInitial() : BigDecimal.ZERO;

        Compte compte = Compte.builder()
                .numeroCompte(numeroCompte)
                .typeCompte(request.getTypeCompte())
                .solde(soldeInitial)
                .proprietaire(client)
                .build();

        Compte savedCompte = compteRepository.save(compte);
        return mapToResponse(savedCompte);
    }

    @Transactional(readOnly = true)
    public CompteResponse getCompteById(Long id) {
        Compte compte = compteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte", "id", id));
        return mapToResponse(compte);
    }

    @Transactional(readOnly = true)
    public CompteResponse getCompteByNumero(String numeroCompte) {
        Compte compte = compteRepository.findByNumeroCompte(numeroCompte)
                .orElseThrow(() -> new ResourceNotFoundException("Compte", "numéro", numeroCompte));
        return mapToResponse(compte);
    }

    @Transactional(readOnly = true)
    public List<CompteResponse> getAllComptes() {
        return compteRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompteResponse> getComptesByClientId(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", "id", clientId);
        }
        return compteRepository.findByProprietaireId(clientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les comptes de l'utilisateur connecté
     */
    @Transactional(readOnly = true)
    public List<CompteResponse> getMesComptes() {
        // Récupérer l'utilisateur connecté via le token JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "username", username));

        // Vérifier si l'utilisateur a un client associé
        if (user.getClient() == null) {
            // Retourner une liste vide si pas de client associé (ex: admin)
            return List.of();
        }

        // Récupérer les comptes du client
        return compteRepository.findByProprietaireId(user.getClient().getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Crée un nouveau compte pour le client connecté
     */
    public CompteResponse creerMonCompte(CompteRequest request) {
        // Récupérer l'utilisateur connecté via le token JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "username", username));

        // Vérifier si l'utilisateur a un client associé
        if (user.getClient() == null) {
            throw new BadRequestException("Votre compte utilisateur n'est pas associé à un profil client");
        }

        Client client = user.getClient();

        // Générer un numéro IBAN unique
        String numeroCompte = genererNumeroCompte();

        // Utiliser le solde initial ou 0 par défaut
        BigDecimal soldeInitial = request.getSoldeInitial() != null ? request.getSoldeInitial() : BigDecimal.ZERO;

        Compte compte = Compte.builder()
                .numeroCompte(numeroCompte)
                .typeCompte(request.getTypeCompte())
                .solde(soldeInitial)
                .proprietaire(client)
                .build();

        Compte savedCompte = compteRepository.save(compte);
        return mapToResponse(savedCompte);
    }

    public CompteResponse effectuerDepot(Long compteId, DepotRetraitRequest request) {
        Compte compte = compteRepository.findById(compteId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte", "id", compteId));

        // Mettre à jour le solde
        compte.setSolde(compte.getSolde().add(request.getMontant()));

        // Créer la transaction
        Transaction transaction = Transaction.builder()
                .typeTransaction(TypeTransaction.DEPOT)
                .montant(request.getMontant())
                .description(request.getDescription() != null ? request.getDescription() : "Dépôt")
                .compteDestination(compte)
                .build();

        transactionRepository.save(transaction);
        Compte updatedCompte = compteRepository.save(compte);

        return mapToResponse(updatedCompte);
    }

    public CompteResponse effectuerRetrait(Long compteId, DepotRetraitRequest request) {
        Compte compte = compteRepository.findById(compteId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte", "id", compteId));

        // Vérifier le solde
        if (compte.getSolde().compareTo(request.getMontant()) < 0) {
            throw new SoldeInsuffisantException(compte.getNumeroCompte(), compte.getSolde(), request.getMontant());
        }

        // Mettre à jour le solde
        compte.setSolde(compte.getSolde().subtract(request.getMontant()));

        // Créer la transaction
        Transaction transaction = Transaction.builder()
                .typeTransaction(TypeTransaction.RETRAIT)
                .montant(request.getMontant())
                .description(request.getDescription() != null ? request.getDescription() : "Retrait")
                .compteSource(compte)
                .build();

        transactionRepository.save(transaction);
        Compte updatedCompte = compteRepository.save(compte);

        return mapToResponse(updatedCompte);
    }

    public void effectuerVirement(VirementRequest request) {
        // Vérifier que les comptes sont différents
        if (request.getNumeroCompteSource().equals(request.getNumeroCompteDestination())) {
            throw new BadRequestException("Le compte source et le compte destination doivent être différents");
        }

        Compte compteSource = compteRepository.findByNumeroCompte(request.getNumeroCompteSource())
                .orElseThrow(() -> new ResourceNotFoundException("Compte source", "numéro",
                        request.getNumeroCompteSource()));

        Compte compteDestination = compteRepository.findByNumeroCompte(request.getNumeroCompteDestination())
                .orElseThrow(() -> new ResourceNotFoundException("Compte destination", "numéro",
                        request.getNumeroCompteDestination()));

        // Vérifier le solde du compte source
        if (compteSource.getSolde().compareTo(request.getMontant()) < 0) {
            throw new SoldeInsuffisantException(compteSource.getNumeroCompte(), compteSource.getSolde(),
                    request.getMontant());
        }

        // Effectuer le virement
        compteSource.setSolde(compteSource.getSolde().subtract(request.getMontant()));
        compteDestination.setSolde(compteDestination.getSolde().add(request.getMontant()));

        // Créer la transaction
        Transaction transaction = Transaction.builder()
                .typeTransaction(TypeTransaction.VIREMENT)
                .montant(request.getMontant())
                .description(request.getDescription() != null ? request.getDescription() : "Virement")
                .compteSource(compteSource)
                .compteDestination(compteDestination)
                .build();

        transactionRepository.save(transaction);
        compteRepository.save(compteSource);
        compteRepository.save(compteDestination);
    }

    public void supprimerCompte(Long id) {
        Compte compte = compteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte", "id", id));

        // Vérifier que le solde est nul avant suppression
        if (compte.getSolde().compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException("Impossible de supprimer un compte avec un solde non nul");
        }

        compteRepository.deleteById(id);
    }

    private String genererNumeroCompte() {
        String iban;
        do {
            iban = Iban.random(CountryCode.FR).toString();
        } while (compteRepository.existsByNumeroCompte(iban));

        return iban;
    }

    private CompteResponse mapToResponse(Compte compte) {
        return CompteResponse.builder()
                .id(compte.getId())
                .numeroCompte(compte.getNumeroCompte())
                .typeCompte(compte.getTypeCompte())
                .dateCreation(compte.getDateCreation())
                .solde(compte.getSolde())
                .clientId(compte.getProprietaire().getId())
                .clientNom(compte.getProprietaire().getNom())
                .clientPrenom(compte.getProprietaire().getPrenom())
                .build();
    }
}
