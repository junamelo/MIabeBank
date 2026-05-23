package com.ega.bank.controller;

import com.ega.bank.dto.request.CompteRequest;
import com.ega.bank.dto.request.DepotRetraitRequest;
import com.ega.bank.dto.request.VirementRequest;
import com.ega.bank.dto.response.CompteResponse;
import com.ega.bank.service.CompteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comptes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CompteController {

    private final CompteService compteService;

    @PostMapping
    public ResponseEntity<CompteResponse> creerCompte(@Valid @RequestBody CompteRequest request) {
        CompteResponse response = compteService.creerCompte(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompteResponse> getCompteById(@PathVariable Long id) {
        CompteResponse response = compteService.getCompteById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/numero/{numeroCompte}")
    public ResponseEntity<CompteResponse> getCompteByNumero(@PathVariable String numeroCompte) {
        CompteResponse response = compteService.getCompteByNumero(numeroCompte);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CompteResponse>> getAllComptes() {
        List<CompteResponse> comptes = compteService.getAllComptes();
        return ResponseEntity.ok(comptes);
    }

    /**
     * Récupère les comptes de l'utilisateur actuellement connecté
     * GET /api/comptes/mes-comptes
     */
    @GetMapping("/mes-comptes")
    public ResponseEntity<List<CompteResponse>> getMesComptes() {
        List<CompteResponse> comptes = compteService.getMesComptes();
        return ResponseEntity.ok(comptes);
    }

    /**
     * Crée un nouveau compte pour le client actuellement connecté
     * POST /api/comptes/mon-compte
     */
    @PostMapping("/mon-compte")
    public ResponseEntity<CompteResponse> creerMonCompte(@Valid @RequestBody CompteRequest request) {
        CompteResponse response = compteService.creerMonCompte(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CompteResponse>> getComptesByClientId(@PathVariable Long clientId) {
        List<CompteResponse> comptes = compteService.getComptesByClientId(clientId);
        return ResponseEntity.ok(comptes);
    }

    @PostMapping("/{id}/depot")
    public ResponseEntity<CompteResponse> effectuerDepot(
            @PathVariable Long id,
            @Valid @RequestBody DepotRetraitRequest request) {
        CompteResponse response = compteService.effectuerDepot(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/retrait")
    public ResponseEntity<CompteResponse> effectuerRetrait(
            @PathVariable Long id,
            @Valid @RequestBody DepotRetraitRequest request) {
        CompteResponse response = compteService.effectuerRetrait(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/virement")
    public ResponseEntity<Void> effectuerVirement(@Valid @RequestBody VirementRequest request) {
        compteService.effectuerVirement(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerCompte(@PathVariable Long id) {
        compteService.supprimerCompte(id);
        return ResponseEntity.noContent().build();
    }
}
