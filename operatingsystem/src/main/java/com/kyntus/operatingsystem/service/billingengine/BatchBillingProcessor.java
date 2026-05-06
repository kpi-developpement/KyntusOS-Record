package com.kyntus.operatingsystem.service.billingengine;

import com.kyntus.operatingsystem.model.PilotRecord;
import com.kyntus.operatingsystem.repository.PilotRecordRepository;
import com.kyntus.operatingsystem.service.billingengine.sav.SavRuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchBillingProcessor {

    private final PilotRecordRepository repository;
    private final RaccRuleEngine raccRuleEngine;
    private final SavRuleEngine savRuleEngine;

    // 🔥 VITESSE MAX: 10,000 par lot!
    private static final int BATCH_SIZE = 10000;

    @Transactional
    public int processMonthBilling(String category, int year, int month) {
        log.info("🚀 [ENGINE V2] Démarrage du Batch Turbo pour {} - {}/{}", category, month, year);

        // 1. NUKE OLD V2 DATA
        log.info("🧹 [ENGINE] Nettoyage : Suppression des fantômes V2...");
        repository.deleteOldV2Records(category, year, month);

        int processedCount = 0;
        int pageNumber = 0;
        Page<PilotRecord> pageData;

        // 2. FETCH & PROCESS EN CHUNKS DE 10K (Anti-OOM)
        log.info("📥 [ENGINE] Lancement du Calcul en Lots de {}...", BATCH_SIZE);

        do {
            Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
            pageData = repository.findV1RecordsPageable(category, year, month, pageable);
            List<PilotRecord> v1Records = pageData.getContent();

            if (v1Records.isEmpty()) {
                break;
            }

            List<PilotRecord> v2Batch = new ArrayList<>();

            for (PilotRecord v1 : v1Records) {
                try {
                    Map<String, Object> v2Data;

                    if ("SAV".equalsIgnoreCase(category)) {
                        v2Data = savRuleEngine.applyBillingRules(v1.getDynamicData());
                    } else if ("RACC".equalsIgnoreCase(category)) {
                        v2Data = raccRuleEngine.applyBillingRules(v1.getDynamicData());
                    } else {
                        v2Data = v1.getDynamicData();
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

                    v2Batch.add(v2);
                } catch (Exception e) {
                    log.error("❌ Erreur calcul pour EPS: {}", v1.getEpsReference(), e);
                }
            }

            // 🔥 BATCH INSERT: Tiri 10,000 sster f da9a we7da!
            repository.saveAll(v2Batch);
            processedCount += v2Batch.size();

            log.info("⚡ [ENGINE] Lot {} terminé. Total calculé: {}", (pageNumber + 1), processedCount);

            // 🧹 Vider la RAM pour le prochain lot
            v2Batch.clear();
            pageNumber++;

        } while (pageData.hasNext());

        log.info("🎯 [ENGINE V2] Fin. {} records V2 générés à la vitesse de l'éclair.", processedCount);
        return processedCount;
    }
}