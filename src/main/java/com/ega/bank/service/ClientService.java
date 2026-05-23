package com.ega.bank.service;

import com.ega.bank.dto.request.ClientRequest;
import com.ega.bank.dto.response.ClientResponse;
import com.ega.bank.entity.Client;
import com.ega.bank.exception.DuplicateResourceException;
import com.ega.bank.exception.ResourceNotFoundException;
import com.ega.bank.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientResponse creerClient(ClientRequest request) {
        // Vérifier si l'email existe déjà
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Client", "email", request.getEmail());
        }

        // Vérifier si le téléphone existe déjà
        if (clientRepository.existsByTelephone(request.getTelephone())) {
            throw new DuplicateResourceException("Client", "téléphone", request.getTelephone());
        }

        Client client = Client.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .dateNaissance(request.getDateNaissance())
                .sexe(request.getSexe())
                .adresse(request.getAdresse())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .nationalite(request.getNationalite())
                .build();

        Client savedClient = clientRepository.save(client);
        return mapToResponse(savedClient);
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));
        return mapToResponse(client);
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> rechercherParNom(String nom) {
        return clientRepository.findByNomContainingIgnoreCase(nom).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ClientResponse modifierClient(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        // Vérifier si l'email est utilisé par un autre client
        clientRepository.findByEmail(request.getEmail())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new DuplicateResourceException("Client", "email", request.getEmail());
                });

        // Vérifier si le téléphone est utilisé par un autre client
        clientRepository.findByTelephone(request.getTelephone())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new DuplicateResourceException("Client", "téléphone", request.getTelephone());
                });

        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom());
        client.setDateNaissance(request.getDateNaissance());
        client.setSexe(request.getSexe());
        client.setAdresse(request.getAdresse());
        client.setTelephone(request.getTelephone());
        client.setEmail(request.getEmail());
        client.setNationalite(request.getNationalite());

        Client updatedClient = clientRepository.save(client);
        return mapToResponse(updatedClient);
    }

    public void supprimerClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client", "id", id);
        }
        clientRepository.deleteById(id);
    }

    private ClientResponse mapToResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .dateNaissance(client.getDateNaissance())
                .sexe(client.getSexe())
                .adresse(client.getAdresse())
                .telephone(client.getTelephone())
                .email(client.getEmail())
                .nationalite(client.getNationalite())
                .nombreComptes(client.getComptes() != null ? client.getComptes().size() : 0)
                .build();
    }
}
