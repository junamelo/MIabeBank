package com.ega.bank.service;

import com.ega.bank.dto.request.LoginRequest;
import com.ega.bank.dto.request.RegisterRequest;
import com.ega.bank.dto.response.AuthResponse;
import com.ega.bank.entity.Client;
import com.ega.bank.entity.User;
import com.ega.bank.exception.BadRequestException;
import com.ega.bank.exception.DuplicateResourceException;
import com.ega.bank.exception.ResourceNotFoundException;
import com.ega.bank.repository.ClientRepository;
import com.ega.bank.repository.UserRepository;
import com.ega.bank.security.JwtService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthService(
            UserRepository userRepository,
            ClientRepository clientRepository,
            @Lazy PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Lazy AuthenticationManager authenticationManager,
            @Lazy UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    public AuthResponse register(RegisterRequest request) {
        // Vérifier si le nom d'utilisateur existe déjà
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Utilisateur", "username", request.getUsername());
        }

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Utilisateur", "email", request.getEmail());
        }

        // Vérifier si l'email client existe déjà
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Client", "email", request.getEmail());
        }

        // Créer le client
        Client client = Client.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .sexe(request.getSexe())
                .nationalite(request.getNationalite())
                .dateNaissance(request.getDateNaissance())
                .email(request.getEmail())
                .adresse(request.getAdresse())
                .telephone(request.getTelephone())
                .build();

        Client savedClient = clientRepository.save(client);

        // Créer le nouvel utilisateur lié au client
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .enabled(true)
                .client(savedClient)
                .build();

        User savedUser = userRepository.save(user);

        // Générer le token JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Nom d'utilisateur ou mot de passe incorrect");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "username", request.getUsername()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
