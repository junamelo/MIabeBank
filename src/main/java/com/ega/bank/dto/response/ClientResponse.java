package com.ega.bank.dto.response;

import com.ega.bank.enums.Sexe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {

    private Long id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private Sexe sexe;
    private String adresse;
    private String telephone;
    private String email;
    private String nationalite;
    private int nombreComptes;
}
