package com.ega.bank.config;

import com.ega.bank.entity.Client;
import com.ega.bank.entity.Compte;
import com.ega.bank.entity.User;
import com.ega.bank.enums.Sexe;
import com.ega.bank.enums.TypeCompte;
import com.ega.bank.repository.ClientRepository;
import com.ega.bank.repository.CompteRepository;
import com.ega.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase(
            ClientRepository clientRepository,
            UserRepository userRepository,
            CompteRepository compteRepository) {

        return args -> {
            // Vérifier si les données existent déjà
            if (userRepository.count() > 0) {
                log.info("Base de données déjà initialisée");
                return;
            }

            log.info("Initialisation des données de test...");

            // Créer les clients
            Client client1 = Client.builder()
                    .nom("Dupont")
                    .prenom("Jean")
                    .email("jean.dupont@email.com")
                    .telephone("0612345678")
                    .adresse("123 Rue de Paris, 75001 Paris")
                    .dateNaissance(LocalDate.of(1985, 3, 15))
                    .nationalite("Française")
                    .sexe(Sexe.MASCULIN)
                    .build();
            client1 = clientRepository.save(client1);

            Client client2 = Client.builder()
                    .nom("Martin")
                    .prenom("Marie")
                    .email("marie.martin@email.com")
                    .telephone("0623456789")
                    .adresse("456 Avenue des Champs, 69001 Lyon")
                    .dateNaissance(LocalDate.of(1990, 7, 22))
                    .nationalite("Française")
                    .sexe(Sexe.FEMININ)
                    .build();
            client2 = clientRepository.save(client2);

            Client client3 = Client.builder()
                    .nom("Bernard")
                    .prenom("Pierre")
                    .email("pierre.bernard@email.com")
                    .telephone("0634567890")
                    .adresse("789 Boulevard Central, 13001 Marseille")
                    .dateNaissance(LocalDate.of(1978, 11, 8))
                    .nationalite("Française")
                    .sexe(Sexe.MASCULIN)
                    .build();
            client3 = clientRepository.save(client3);

            log.info("Clients créés: {}", clientRepository.count());

            // Créer les utilisateurs avec mots de passe uniques encodés
            User admin = User.builder()
                    .username("admin")
                    .email("admin@egabank.com")
                    .password(passwordEncoder.encode("admin2026"))
                    .role("ADMIN")
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(admin);

            User user1 = User.builder()
                    .username("jean.dupont")
                    .email("jean.dupont@email.com")
                    .password(passwordEncoder.encode("jean1234"))
                    .role("USER")
                    .enabled(true)
                    .client(client1)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user1);

            User user2 = User.builder()
                    .username("marie.martin")
                    .email("marie.martin@email.com")
                    .password(passwordEncoder.encode("marie1234"))
                    .role("USER")
                    .enabled(true)
                    .client(client2)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user2);

            User user3 = User.builder()
                    .username("pierre.bernard")
                    .email("pierre.bernard@email.com")
                    .password(passwordEncoder.encode("pierre1234"))
                    .role("USER")
                    .enabled(true)
                    .client(client3)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user3);

            log.info("Utilisateurs créés: {}", userRepository.count());

            // Créer les comptes bancaires
            Compte compte1 = Compte.builder()
                    .numeroCompte("FR7630001007941234567890185")
                    .typeCompte(TypeCompte.COURANT)
                    .solde(new BigDecimal("2500.00"))
                    .dateCreation(LocalDateTime.now())
                    .proprietaire(client1)
                    .build();
            compteRepository.save(compte1);

            Compte compte2 = Compte.builder()
                    .numeroCompte("FR7630001007941234567890186")
                    .typeCompte(TypeCompte.EPARGNE)
                    .solde(new BigDecimal("15000.00"))
                    .dateCreation(LocalDateTime.now())
                    .proprietaire(client1)
                    .build();
            compteRepository.save(compte2);

            Compte compte3 = Compte.builder()
                    .numeroCompte("FR7630001007941234567890187")
                    .typeCompte(TypeCompte.COURANT)
                    .solde(new BigDecimal("3200.50"))
                    .dateCreation(LocalDateTime.now())
                    .proprietaire(client2)
                    .build();
            compteRepository.save(compte3);

            Compte compte4 = Compte.builder()
                    .numeroCompte("FR7630001007941234567890188")
                    .typeCompte(TypeCompte.COURANT)
                    .solde(new BigDecimal("850.75"))
                    .dateCreation(LocalDateTime.now())
                    .proprietaire(client3)
                    .build();
            compteRepository.save(compte4);

            Compte compte5 = Compte.builder()
                    .numeroCompte("FR7630001007941234567890189")
                    .typeCompte(TypeCompte.EPARGNE)
                    .solde(new BigDecimal("22000.00"))
                    .dateCreation(LocalDateTime.now())
                    .proprietaire(client3)
                    .build();
            compteRepository.save(compte5);

            log.info("Comptes créés: {}", compteRepository.count());
            log.info("=== Initialisation terminée ===");
            log.info("Identifiants de test:");
            log.info("  - jean.dupont / jean1234");
            log.info("  - marie.martin / marie1234");
            log.info("  - pierre.bernard / pierre1234");
            log.info("  - admin / admin2026");
        };
    }
}
