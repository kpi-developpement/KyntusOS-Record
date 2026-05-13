package com.kyntus.operatingsystem.service.record;

import com.kyntus.operatingsystem.model.PilotRecord;
import com.kyntus.operatingsystem.repository.PilotRecordRepository;
import com.kyntus.operatingsystem.service.billingengine.BatchBillingProcessor;
import com.kyntus.operatingsystem.service.billingengine.RaccRuleEngine;
import com.kyntus.operatingsystem.service.billingengine.sav.SavRuleEngine;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // 🔥 L'IMPORT JDID LI ZEDNA BACH Y-RETTEB
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordV1Service {

    private final PilotRecordRepository repository;
    private final BatchBillingProcessor batchProcessor;
    private final RaccRuleEngine raccRuleEngine;
    private final SavRuleEngine savRuleEngine;
    private final EntityManager entityManager;

    public List<String> getAvailableColumns(String category, int year, int month, String version) {
        return repository.findDistinctDynamicColumnsFast(category, year, month, version);
    }

    public Page<PilotRecord> getV1RecordsFiltered(String category, int year, int month, Pageable pageable, List<String> selectedColumns, String version) {
        return repository.findRecordsByCategoryDateAndVersion(category, year, month, version, pageable);
    }

    // ==========================================================
    // 🔥 MOTEUR D'AUTO-CALCUL MULTI-MOIS
    // ==========================================================
    public void calculatePeriod(String category, int startYear, int startMonth, int endYear, int endMonth) {
        int currentYear = startYear;
        int currentMonth = startMonth;

        log.info("🚀 [AUTO-CALC] Démarrage du calcul V2 de {}/{} à {}/{} pour {}", startMonth, startYear, endMonth, endYear, category);

        while ((currentYear * 100 + currentMonth) <= (endYear * 100 + endMonth)) {
            log.info("⚙️ [AUTO-CALC] Exécution du Batch pour : {}/{}", currentMonth, currentYear);
            batchProcessor.processMonthBilling(category, currentYear, currentMonth);

            currentMonth++;
            if (currentMonth > 12) {
                currentMonth = 1;
                currentYear++;
            }
        }
        log.info("✅ [AUTO-CALC] Fin des calculs pour la période.");
    }

    // ==========================================================
    // 🔥 THE ZERO-WRITE ENGINE D'EXPORT CSV (ON-THE-FLY + ESSENTIALS ONLY)
    // ==========================================================
    public byte[] generateCsvExportOnTheFly(String category, int startYear, int startMonth, int endYear, int endMonth) {
        int startPeriod = startYear * 100 + startMonth;
        int endPeriod = endYear * 100 + endMonth;

        log.info("📥 [ZERO-WRITE EXPORT] Démarrage de l'export à la volée pour {} - Période: {} à {}", category, startPeriod, endPeriod);

        // 🔥 LA LISTE DES COLONNES ESSENTIELLES
        List<String> colonnesEssentielles = Arrays.asList(
                // 1. Inputs de base
                "INSTALLATION", "MATERIEL", "MES", "SUPPORT", "LOGISTIQUE", "DEPLACEMENT",

                // 2. Part Kyntus
                "Forfait INST Kyntus", "Prix forfait INST Kyntus",
                "Forfait INST Support Kyntus ", "Prix Forfait INST support Kyntus",
                "Forfait INTST- Kyntus", "Prix Forfait INTST Kyntus",
                "Materiel prix2", "MES22 Kyntus", "Prix Forfait MES Kyntus", "Mt Kyntus",

                // 3. Part Sous-Traitant (SST)
                "Forfait INST SST", "Prix Forfait SST", "Materiel prix",
                "MES STT", "Prix Forfait MES SST", "Forfait Logistique SST",
                "Prix Forfait Logistique SST", "Mt SST"
        );

        StringBuilder csv = new StringBuilder();

        // 1. L'Header dyal l'Excel
        csv.append("ID;EPS_REFERENCE;CATEGORY;YEAR;MONTH;VERSION;");
        for (String col : colonnesEssentielles) {
            csv.append(col).append(";");
        }
        csv.append("\n");

        // 2. Pagination w Calcul fl RAM
        int pageNumber = 0;
        int pageSize = 5000;
        Page<PilotRecord> pageData;

        do {
            // 🔥 THE FIX: Zedna Sort.by("id").ascending() bach l'base de données te3tina dakchi mretteb
            // Hakka moustahil y-dupliqui chi EPS awla y-zgel chi wa7ed!
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").ascending());

            pageData = repository.findV1RecordsForExportPageable(category, startPeriod, endPeriod, pageable);

            for (PilotRecord v1Record : pageData.getContent()) {

                // Calcul Dynamique
                Map<String, Object> v2Data;
                if ("SAV".equalsIgnoreCase(category)) {
                    v2Data = savRuleEngine.applyBillingRules(v1Record.getDynamicData());
                } else if ("RACC".equalsIgnoreCase(category)) {
                    v2Data = raccRuleEngine.applyBillingRules(v1Record.getDynamicData());
                } else {
                    v2Data = v1Record.getDynamicData();
                }

                // Info Statiques
                csv.append(v1Record.getId() != null ? v1Record.getId() : "").append(";");
                csv.append(v1Record.getEpsReference() != null ? v1Record.getEpsReference() : "").append(";");
                csv.append(v1Record.getCategory() != null ? v1Record.getCategory() : "").append(";");
                csv.append(v1Record.getImportYear() != null ? v1Record.getImportYear() : "").append(";");
                csv.append(v1Record.getImportMonth() != null ? v1Record.getImportMonth() : "").append(";");
                csv.append("V2;");

                // Info Dynamiques (Filtre par liste essentielle)
                for (String col : colonnesEssentielles) {
                    if (v2Data != null && v2Data.containsKey(col)) {
                        Object val = v2Data.get(col);
                        String valStr = val != null ? val.toString().replace(";", ",") : "";
                        csv.append(valStr).append(";");
                    } else {
                        csv.append(";"); // Vide ila makantch
                    }
                }
                csv.append("\n");
            }

            log.info("✅ [ZERO-WRITE EXPORT] Lot {} calculé et exporté ({} lignes)", pageNumber + 1, pageData.getNumberOfElements());
            pageNumber++;

            // N-khewiw l'RAM dyal Hibernate be3d kola lot!
            entityManager.clear();

        } while (pageData.hasNext());

        // 3. BOM l'UTF-8
        byte[] bom = new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF };
        byte[] csvBytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] finalBytes = new byte[bom.length + csvBytes.length];

        System.arraycopy(bom, 0, finalBytes, 0, bom.length);
        System.arraycopy(csvBytes, 0, finalBytes, bom.length, csvBytes.length);

        log.info("🎉 [ZERO-WRITE EXPORT] Fichier CSV généré avec succès !");
        return finalBytes;
    }
}