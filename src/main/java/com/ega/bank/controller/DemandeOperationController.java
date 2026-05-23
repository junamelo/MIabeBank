package com.ega.bank.controller;

import com.ega.bank.dto.request.DemandeOperationRequest;
import com.ega.bank.dto.response.DemandeOperationResponse;
import com.ega.bank.entity.User;
import com.ega.bank.exception.BadRequestException;
import com.ega.bank.repository.UserRepository;
import com.ega.bank.service.DemandeOperationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DemandeOperationController {

    private final DemandeOperationService demandeService;
    private final UserRepository userRepository;

    /**
     * Créer une nouvelle demande (client)
     */
    @PostMapping
    public ResponseEntity<DemandeOperationResponse> creerDemande(
            @Valid @RequestBody DemandeOperationRequest request) {

        Long clientId = getCurrentClientId();
        DemandeOperationResponse response = demandeService.creerDemande(request, clientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer mes demandes (client connecté)
     */
    @GetMapping("/mes-demandes")
    public ResponseEntity<List<DemandeOperationResponse>> getMesDemandes() {
        Long clientId = getCurrentClientId();
        List<DemandeOperationResponse> demandes = demandeService.getDemandesByClient(clientId);
        return ResponseEntity.ok(demandes);
    }

    /**
     * Récupérer toutes les demandes en attente (admin)
     */
    @GetMapping("/en-attente")
    public ResponseEntity<List<DemandeOperationResponse>> getDemandesEnAttente() {
        List<DemandeOperationResponse> demandes = demandeService.getDemandesEnAttente();
        return ResponseEntity.ok(demandes);
    }

    /**
     * Récupérer toutes les demandes (admin)
     */
    @GetMapping
    public ResponseEntity<List<DemandeOperationResponse>> getAllDemandes() {
        List<DemandeOperationResponse> demandes = demandeService.getAllDemandes();
        return ResponseEntity.ok(demandes);
    }

    /**
     * Compter les demandes en attente (admin - pour le badge)
     */
    @GetMapping("/count-en-attente")
    public ResponseEntity<Map<String, Long>> countDemandesEnAttente() {
        long count = demandeService.countDemandesEnAttente();
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Récupérer une demande par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DemandeOperationResponse> getDemandeById(@PathVariable Long id) {
        DemandeOperationResponse response = demandeService.getDemandeById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Approuver une demande (admin)
     */
    @PostMapping("/{id}/approuver")
    public ResponseEntity<DemandeOperationResponse> approuverDemande(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        String commentaire = body != null ? body.get("commentaire") : null;
        DemandeOperationResponse response = demandeService.approuverDemande(id, commentaire);
        return ResponseEntity.ok(response);
    }

    /**
     * Rejeter une demande (admin)
     */
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<DemandeOperationResponse> rejeterDemande(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String motif = body.get("motif");
        if (motif == null || motif.isBlank()) {
            throw new BadRequestException("Le motif de rejet est obligatoire");
        }
        DemandeOperationResponse response = demandeService.rejeterDemande(id, motif);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer l'ID du client connecté
     */
    private Long getCurrentClientId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Utilisateur non trouvé"));

        if (user.getClient() == null) {
            throw new BadRequestException("Cet utilisateur n'est pas un client");
        }

        return user.getClient().getId();
    }
}
