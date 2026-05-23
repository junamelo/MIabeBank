package com.ega.bank.controller;

import com.ega.bank.dto.response.StatsResponse;
import com.ega.bank.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatsController {

    private final StatsService statsService;

    /**
     * Récupère toutes les statistiques du dashboard admin
     * GET /api/admin/stats
     */
    @GetMapping
    public ResponseEntity<StatsResponse> getDashboardStats() {
        StatsResponse stats = statsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
}
