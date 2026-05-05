package com.kyntus.operatingsystem.service.billingengine.sav;

import com.kyntus.operatingsystem.service.billingengine.BpuEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavRuleEngine {

    private final SavBpuCacheService bpuCache;

    public Map<String, Object> applyBillingRules(Map<String, Object> existingData) {
        Map<String, Object> processedData = new HashMap<>(existingData);

        log.info("===============================================================");
        log.info("🔍 [DEEP SCAN SAV] Extraction des Inputs...");

        // --- EXTRACTION FLEXIBLE (AO, AP) ---
        double valAO = getDoubleFlexible(existingData, "INSTALLATION"); // AO2
        double valAP = getDoubleFlexible(existingData, "DEPLACEMENT");  // AP2

        log.info("📥 INPUTS LUS -> INSTALLATION: {}, DEPLACEMENT: {}", valAO, valAP);

        // --- LOOKUPS (RECHERCHEV) ---
        BpuEntry inst = bpuCache.lookupByPrice(valAO); // AR & AS
        BpuEntry dep = bpuCache.lookupByPrice(valAP);  // AT & AU
        BpuEntry zeroEntry = new BpuEntry("SAV-0.0", 0.0, 0.0); // L'Ziro dima kay3ti SAV-0.0

        // =========================================================
        // 📊 COLONNES KYNTUS (AR -> BA)
        // =========================================================

        // AR : =RECHERCHEV(AO2,'BPU SAV'!D:E,2,FAUX)
        processedData.put("Forfait INST Kyntus", inst.getCodeForfait());
        // AS : =RECHERCHEV(AR2,'BPU SAV'!C:D,2,FAUX)
        processedData.put("Prix forfait INST Kyntus", inst.getPrixKyntus());

        // AT : =RECHERCHEV(AP2,'BPU SAV'!D:E,2,FAUX)
        processedData.put("Forfait INST Support Kyntus ", dep.getCodeForfait()); // Attention l'espace hna mn l'Excel
        // AU : =RECHERCHEV(AT2,'BPU SAV'!C:D,2,FAUX)
        processedData.put("Prix Forfait INST support Kyntus", dep.getPrixKyntus());

        // AV : SAV-0.0
        processedData.put("Forfait INTST- Kyntus ", zeroEntry.getCodeForfait()); // Attention l'espace
        // AW : =RECHERCHEV(AV2,'BPU SAV'!C:D,2,FAUX)
        processedData.put("Prix Forfait INTST Kyntus", zeroEntry.getPrixKyntus());

        // AX : 0
        processedData.put("Materiel prix2", 0.0);

        // AY : SAV-0.0
        processedData.put("MES22 Kyntus", zeroEntry.getCodeForfait());
        // AZ : =RECHERCHEV(AY2,'BPU SAV'!C:D,2,FAUX)
        processedData.put("Prix Forfait MES Kyntus", zeroEntry.getPrixKyntus());

        // BA : =AZ2+AX2+AW2+AU2+AS2
        double ba = zeroEntry.getPrixKyntus() + 0.0 + zeroEntry.getPrixKyntus() + dep.getPrixKyntus() + inst.getPrixKyntus();
        processedData.put("Mt Kyntus", ba);

        // JARRETIERE (Statique)
        processedData.put("JARRETIERE_UNI", "0");
        processedData.put("PRIX_HT_JARRETIERE2", 0.0);

        // =========================================================
        // 📊 COLONNES SOUS-TRAITANT (BD -> BK)
        // =========================================================

        // BD : =AR2
        processedData.put("Forfait INST SST", inst.getCodeForfait());
        // BE : =RECHERCHEV(BD2,'BPU SAV'!E:F,2,FAUX)
        processedData.put("Prix Forfait SST", inst.getPrixSst());

        // BF : 0
        processedData.put("Materiel prix", 0.0);

        // BG : SAV-0.0
        processedData.put("MES STT", zeroEntry.getCodeForfait());
        // BH : =RECHERCHEV(BG2,'BPU SAV'!E:F,2,FAUX)
        processedData.put("Prix Forfait MES SST", zeroEntry.getPrixSst());

        // BI : =AT2
        processedData.put("Forfait Logistique SST", dep.getCodeForfait());
        // BJ : =RECHERCHEV(BI2,'BPU SAV'!E:F,2,FAUX)
        processedData.put("Prix Forfait Logistique SST", dep.getPrixSst());

        // BK : =BJ2+BH2+BF2+BE2
        double bk = dep.getPrixSst() + zeroEntry.getPrixSst() + 0.0 + inst.getPrixSst();
        processedData.put("Mt SST", bk);

        log.info("💵 [RESULTAT SAV] TOTAL KYNTUS: {} | TOTAL SST: {}", ba, bk);
        log.info("===============================================================");

        return processedData;
    }

    private double getDoubleFlexible(Map<String, Object> data, String targetKey) {
        String cleanTarget = targetKey.trim().toLowerCase();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getKey().trim().toLowerCase().equals(cleanTarget)) {
                Object val = entry.getValue();
                if (val == null || val.toString().trim().isEmpty()) return 0.0;
                try {
                    return Double.parseDouble(val.toString().trim().replace(",", "."));
                } catch (Exception e) {
                    return 0.0;
                }
            }
        }
        return 0.0;
    }
}