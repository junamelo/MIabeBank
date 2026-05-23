package com.ega.bank.repository;

import com.ega.bank.entity.Client;
import com.ega.bank.entity.DemandeOperation;
import com.ega.bank.enums.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeOperationRepository extends JpaRepository<DemandeOperation, Long> {

    // Trouver toutes les demandes d'un client
    List<DemandeOperation> findByClientOrderByDateCreationDesc(Client client);

    // Trouver les demandes par statut
    List<DemandeOperation> findByStatutOrderByDateCreationDesc(StatutDemande statut);

    // Trouver toutes les demandes en attente
    List<DemandeOperation> findByStatutOrderByDateCreationAsc(StatutDemande statut);

    // Compter les demandes en attente
    long countByStatut(StatutDemande statut);

    // Trouver toutes les demandes triées par date
    List<DemandeOperation> findAllByOrderByDateCreationDesc();

    // Trouver les demandes d'un client par statut
    List<DemandeOperation> findByClientAndStatutOrderByDateCreationDesc(Client client, StatutDemande statut);
}
