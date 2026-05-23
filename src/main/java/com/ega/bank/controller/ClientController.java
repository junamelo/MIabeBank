package com.ega.bank.controller;

import com.ega.bank.dto.request.ClientRequest;
import com.ega.bank.dto.response.ClientResponse;
import com.ega.bank.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponse> creerClient(@Valid @RequestBody ClientRequest request) {
        ClientResponse response = clientService.creerClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable Long id) {
        ClientResponse response = clientService.getClientById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        List<ClientResponse> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<ClientResponse>> rechercherParNom(@RequestParam String nom) {
        List<ClientResponse> clients = clientService.rechercherParNom(nom);
        return ResponseEntity.ok(clients);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> modifierClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request) {
        ClientResponse response = clientService.modifierClient(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerClient(@PathVariable Long id) {
        clientService.supprimerClient(id);
        return ResponseEntity.noContent().build();
    }
}
