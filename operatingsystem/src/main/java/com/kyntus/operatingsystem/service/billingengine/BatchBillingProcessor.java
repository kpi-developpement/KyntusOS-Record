package com.kyntus.operatingsystem.service.billingengine;

import com.kyntus.operatingsystem.model.PilotRecord;
import com.kyntus.operatingsystem.repository.PilotRecordRepository;
import com.kyntus.operatingsystem.service.billingengine.sav.SavRuleEngine; // 🔥 Import dyal SAV
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchBillingProcessor {

    private final PilotRecordRepository repository;
    private final RaccRuleEngine raccRuleEngine; // 👈 Smiytnaha raccRuleEngine bach tban mzyan
    private final SavRuleEngine savRuleEngine;   // 👈 Zidna l'moteur jdid dyal SAV

    @Transactional
    public int processMonthBilling(String category, int year, int month) {
        log.info("🚀 [ENGINE] Démarrage du Batch pour {} - {}/{}", category, month, year);

        // 1. NUKE OLD V2 DATA (Msse7 l'khelta dyal l'passé)
        log.info("🧹 [ENGINE] Nettoyage : Suppression des fantômes V2...");
        repository.deleteOldV2Records(category, year, month);
        log.info("✅ [ENGINE] Base de données nettoyée.");

        // 2. FETCH V1 DATA (Jbed ghir s7i7)
        List<PilotRecord> v1Records = repository.findAllV1RecordsByCategoryAndDate(category, year, month);
        if (v1Records.isEmpty()) {
            log.warn("⚠️ [ENGINE] Aucune donnée V1 trouvée pour {} {}/{}", category, month, year);
            return 0;
        }
        log.info("📥 [ENGINE] {} records V1 trouvés. Lancement du Calcul...", v1Records.size());

        int processedCount = 0;

        // 3. PROCESS AND SAVE
        for (PilotRecord v1 : v1Records) {
            try {
                Map<String, Object> v2Data;

                // 🔥 THE MAGIC ROUTER : Kay-fwwez l'Matrix 3la 7ssab l'Category
                if ("SAV".equalsIgnoreCase(category)) {
                    v2Data = savRuleEngine.applyBillingRules(v1.getDynamicData());
                } else if ("RACC".equalsIgnoreCase(category)) {
                    v2Data = raccRuleEngine.applyBillingRules(v1.getDynamicData());
                } else {
                    log.warn("⚠️ [ENGINE] Catégorie inconnue ({}), copie des données V1 sans modif.", category);
                    v2Data = v1.getDynamicData(); // Ila makanch RACC/SAV, kay-kheli l'data kima hiya
                }

                PilotRecord v2 = new PilotRecord();
                v2.setEpsReference(v1.getEpsReference());
                v2.setVersion("V2");
                v2.setDynamicData(v2Data);
                v2.setImportedAt(LocalDateTime.now());
                v2.setCategory(v1.getCategory());
                v2.setImportYear(v1.getImportYear());
                v2.setImportMonth(v1.getImportMonth());
                v2.setPilotId(v1.getPilotId());
                v2.setFileRank(v1.getFileRank());
                v2.setSourceFile("KYNTUS_BILLING_ENGINE");

                repository.save(v2);
                processedCount++;
            } catch (Exception e) {
                log.error("❌ Erreur calcul pour EPS: {}", v1.getEpsReference(), e);
            }
        }

        log.info("🎯 [ENGINE] Fin. {} records V2 générés 100% Pures.", processedCount);
        return processedCount;
    }
}