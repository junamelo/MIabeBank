package com.ega.bank.repository;

import com.ega.bank.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByEmail(String email);

    Optional<Client> findByTelephone(String telephone);

    List<Client> findByNomContainingIgnoreCase(String nom);

    List<Client> findByPrenomContainingIgnoreCase(String prenom);

    List<Client> findByNationalite(String nationalite);

    boolean existsByEmail(String email);

    boolean existsByTelephone(String telephone);
}
