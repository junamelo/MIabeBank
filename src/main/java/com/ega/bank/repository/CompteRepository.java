package com.ega.bank.repository;

import com.ega.bank.entity.Compte;
import com.ega.bank.enums.TypeCompte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompteRepository extends JpaRepository<Compte, Long> {

    Optional<Compte> findByNumeroCompte(String numeroCompte);

    List<Compte> findByProprietaireId(Long clientId);

    List<Compte> findByTypeCompte(TypeCompte typeCompte);

    List<Compte> findByProprietaireIdAndTypeCompte(Long clientId, TypeCompte typeCompte);

    boolean existsByNumeroCompte(String numeroCompte);

    long countByProprietaireId(Long clientId);

    // Compter par type de compte
    long countByTypeCompte(TypeCompte typeCompte);
}
