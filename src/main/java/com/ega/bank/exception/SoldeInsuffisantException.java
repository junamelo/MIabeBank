package com.ega.bank.exception;

public class SoldeInsuffisantException extends RuntimeException {

    public SoldeInsuffisantException(String message) {
        super(message);
    }

    public SoldeInsuffisantException(String numeroCompte, java.math.BigDecimal solde, java.math.BigDecimal montant) {
        super(String.format("Solde insuffisant sur le compte %s. Solde actuel: %.2f, Montant demandé: %.2f",
                numeroCompte, solde, montant));
    }
}
