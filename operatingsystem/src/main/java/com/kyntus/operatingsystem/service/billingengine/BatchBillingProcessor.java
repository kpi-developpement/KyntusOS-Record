package com.kyntus.operatingsystem.service.billingengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyntus.operatingsystem.model.PilotRecord;
import com.kyntus.operatingsystem.repository.PilotRecordRepository;
import com.kyntus.operatingsystem.service.billingengine.sav.SavRuleEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
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

    // 🔥 THE V8 COMPONENTS: JdbcTemplate l'Insertion d'lber9, w ObjectMapper l'JSONB
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    // 10,000 par lot bach n-tiriw f da9a we7da!
    private static final int BATCH_SIZE = 10000;

    @Transactional
    public int processMonthBilling(String category, int year, int month) {
        log.info("🚀 [ENGINE V3 SUPERSONIC] Démarrage du Batch Turbo pour {} - {}/{}", category, month, year);

        // 1. NUKE OLD V2 DATA
        log.info("🧹 [ENGINE] Nettoyage : Suppression des fantômes V2...");
        repository.deleteOldV2Records(category, year, month);

        int processedCount = 0;
        int pageNumber = 0;
        Page<PilotRecord> pageData;

        // 🔥 THE RAW SQL INSERT (Kay-By-passi Hibernate 100%)
        String sqlInsert = "INSERT INTO pilot_records (eps_reference, dynamic_data, version, imported_at, pilot_id, import_year, import_month, category, source_file, file_rank) " +
                "VALUES (?, ?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?)";

        log.info("📥 [ENGINE] Lancement du Calcul en Lots de {}...", BATCH_SIZE);

        do {
            Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
            pageData = repository.findV1RecordsPageable(category, year, month, pageable);
            List<PilotRecord> v1Records = pageData.getContent();

            if (v1Records.isEmpty()) {
                break;
            }

            List<Object[]> batchArgs = new ArrayList<>();
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

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

                    // N-7ouwlou l'Map l'JSON String bach t-dkhol nishan f l'Colonne JSONB dyal PostgreSQL
                    String dynamicDataJson = objectMapper.writeValueAsString(v2Data);

                    // N-wejdou Sster l'SQL
                    batchArgs.add(new Object[] {
                            v1.getEpsReference(),
                            dynamicDataJson,
                            "V2",
                            now,
                            v1.getPilotId(),
                            v1.getImportYear(),
                            v1.getImportMonth(),
                            v1.getCategory(),
                            "KYNTUS_BILLING_ENGINE",
                            v1.getFileRank()
                    });

                } catch (Exception e) {
                    log.error("❌ Erreur calcul pour EPS: {}", v1.getEpsReference(), e);
                }
            }

            // 🔥 BATCH INSERT: Tiri 10,000 ligne f d99a we7da b l'JDBC l'assli!
            long startTime = System.currentTimeMillis();
            jdbcTemplate.batchUpdate(sqlInsert, batchArgs);
            long endTime = System.currentTimeMillis();

            processedCount += batchArgs.size();

            log.info("⚡ [ENGINE] Lot {} terminé ({} lignes insérées en {} ms). Total calculé: {}",
                    (pageNumber + 1), batchArgs.size(), (endTime - startTime), processedCount);

            // 🧹 N-khewiw l'RAM l'lot jdid
            batchArgs.clear();
            pageNumber++;

        } while (pageData.hasNext());

        log.info("🎯 [ENGINE V3 SUPERSONIC] Fin. {} records V2 générés et insérés en BULK.", processedCount);
        return processedCount;
    }
}