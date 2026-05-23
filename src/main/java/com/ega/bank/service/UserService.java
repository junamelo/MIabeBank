package com.ega.bank.service;

import com.ega.bank.dto.request.UpdateUserRequest;
import com.ega.bank.dto.response.UserResponse;
import com.ega.bank.entity.Client;
import com.ega.bank.entity.User;
import com.ega.bank.exception.BadRequestException;
import com.ega.bank.exception.DuplicateResourceException;
import com.ega.bank.exception.ResourceNotFoundException;
import com.ega.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Récupère l'utilisateur actuellement connecté via le token JWT
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "username", username));
    }

    /**
     * Récupère le profil de l'utilisateur connecté
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        User user = getCurrentUser();
        return mapToResponse(user);
    }

    /**
     * Récupère un utilisateur par son ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdWithClient(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        return mapToResponse(user);
    }

    /**
     * Récupère tous les utilisateurs (admin)
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllWithClient().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une entité User en UserResponse
     */
    private UserResponse mapToResponse(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt());

        // Ajouter les informations du client si lié
        Client client = user.getClient();
        if (client != null) {
            builder.clientId(client.getId())
                    .clientNom(client.getNom())
                    .clientPrenom(client.getPrenom())
                    .clientEmail(client.getEmail())
                    .clientTelephone(client.getTelephone());
        }

        return builder.build();
    }

    /**
     * Met à jour le profil de l'utilisateur connecté
     */
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        User user = getCurrentUser();

        // Mise à jour du username si fourni
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            // Vérifier si le nouveau username n'est pas déjà utilisé
            userRepository.findByUsername(request.getUsername())
                    .filter(u -> !u.getId().equals(user.getId()))
                    .ifPresent(u -> {
                        throw new DuplicateResourceException("Utilisateur", "username", request.getUsername());
                    });
            user.setUsername(request.getUsername());
        }

        // Mise à jour de l'email si fourni
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Vérifier si le nouvel email n'est pas déjà utilisé
            userRepository.findByEmail(request.getEmail())
                    .filter(u -> !u.getId().equals(user.getId()))
                    .ifPresent(u -> {
                        throw new DuplicateResourceException("Utilisateur", "email", request.getEmail());
                    });
            user.setEmail(request.getEmail());
        }

        // Mise à jour du mot de passe si fourni
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            // Vérifier que l'ancien mot de passe est fourni et correct
            if (request.getOldPassword() == null || request.getOldPassword().isBlank()) {
                throw new BadRequestException("L'ancien mot de passe est requis pour changer le mot de passe");
            }
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new BadRequestException("L'ancien mot de passe est incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }
}
