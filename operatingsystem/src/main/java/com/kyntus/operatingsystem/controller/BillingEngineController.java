package com.kyntus.operatingsystem.service.billingengine;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/os/billing-engine")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BillingEngineController {

    private final BatchBillingProcessor batchProcessor;

    // Endpoint li kay-déclenchi l'calcul dyal les factures l mois kaml
    @PostMapping("/execute")
    public ResponseEntity<?> executeBatch(
            @RequestParam String category,
            @RequestParam int year,
            @RequestParam int month) {

        long startTime = System.currentTimeMillis();
        int recordsProcessed = batchProcessor.processMonthBilling(category, year, month);
        long endTime = System.currentTimeMillis();

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Facturation calculée avec succès.",
                "records_processed", recordsProcessed,
                "execution_time_ms", (endTime - startTime)
        ));
    }
}