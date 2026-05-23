package com.ega.bank.controller;

import com.ega.bank.dto.response.TransactionResponse;
import com.ega.bank.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        List<TransactionResponse> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/compte/{compteId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByCompteId(@PathVariable Long compteId) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByCompteId(compteId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/compte/{compteId}/periode")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByCompteAndPeriode(
            @PathVariable Long compteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByCompteAndPeriode(
                compteId, dateDebut, dateFin);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByClientId(@PathVariable Long clientId) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByClientId(clientId);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{id}/annuler")
    public ResponseEntity<TransactionResponse> annulerTransaction(@PathVariable Long id) {
        TransactionResponse response = transactionService.annulerTransaction(id);
        return ResponseEntity.ok(response);
    }
}
