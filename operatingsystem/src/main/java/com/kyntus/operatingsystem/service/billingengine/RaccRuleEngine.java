package com.kyntus.operatingsystem.service.billingengine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaccRuleEngine {

    private final BpuCacheService bpuCache;

    public Map<String, Object> applyBillingRules(Map<String, Object> existingData) {
        Map<String, Object> processedData = new HashMap<>(existingData);

        // --- EXTRACTION FLEXIBLE (X, Y, Z, AA, AB) ---
        double valX = getDoubleFlexible(existingData, "INSTALLATION"); // X
        double valY = getDoubleFlexible(existingData, "MATERIEL");     // Y
        double valZ = getDoubleFlexible(existingData, "MES");          // Z
        double valAA = getDoubleFlexible(existingData, "SUPPORT");     // AA
        double valAB = getDoubleFlexible(existingData, "LOGISTIQUE");  // AB

        // --- LOOKUPS (RECHERCHEV) ---
        // F l'Excel derti RECHERCHEV 3la X, AA, AB, Z
        BpuEntry inst = bpuCache.lookupByPrice(valX, "INST");
        BpuEntry sup = bpuCache.lookupByPrice(valAA, "INST");
        BpuEntry logEntry = bpuCache.lookupByPrice(valAB, "INST");
        BpuEntry mes = bpuCache.lookupByPrice(valZ, "MES");

        // =========================================================
        // 📊 COLONNES KYNTUS (AI -> AR) -- Identique l'Excel dyalek
        // =========================================================

        // AI : =RECHERCHEV(X2,'BPU RACC'!D:E,2,FAUX)
        processedData.put("Forfait INST Kyntus", inst.getCodeForfait());

        // AJ : =RECHERCHEV(AI2,'BPU RACC'!C:D,2,FAUX)  (C'est l'équivalent dyal Prix Kyntus d l'Installation)
        processedData.put("Prix forfait INST Kyntus", inst.getPrixKyntus());

        // AK : =RECHERCHEV(AA2,'BPU RACC'!D:E,2,FAUX)
        processedData.put("Forfait INST Support Kyntus", sup.getCodeForfait());

        // AL : =RECHERCHEV(AK2,'BPU RACC'!C:D,2,FAUX)
        processedData.put("Prix Forfait INST support Kyntus", sup.getPrixKyntus());

        // AM : =RECHERCHEV(AB2,'BPU RACC'!D:E,2,FAUX)
        processedData.put("Forfait INTST- Kyntus", logEntry.getCodeForfait());

        // AN : =RECHERCHEV(AM2,'BPU RACC'!C:D,2,FAUX)
        processedData.put("Prix Forfait INTST Kyntus", logEntry.getPrixKyntus());

        // AO : =Y2
        processedData.put("Materiel prix2", valY);

        // AP : =RECHERCHEV(Z2,'BPU RACC'!D:E,2,FAUX)
        processedData.put("MES22 Kyntus", mes.getCodeForfait());

        // AQ : =RECHERCHEV(AP2,'BPU RACC'!C:D,2,FAUX)
        processedData.put("Prix Forfait MES Kyntus", mes.getPrixKyntus());

        // AR : =AQ2+AO2+AN2+AL2+AJ2
        double ar = mes.getPrixKyntus() + valY + logEntry.getPrixKyntus() + sup.getPrixKyntus() + inst.getPrixKyntus();
        processedData.put("Mt Kyntus", ar);

        // =========================================================
        // 📊 COLONNES SOUS-TRAITANT (AV -> BC) -- Identique l'Excel
        // =========================================================

        // AV : =AI2
        processedData.put("Forfait INST SST", inst.getCodeForfait());

        // AW : =RECHERCHEV(AV2,'BPU RACC'!E:F,2,FAUX)
        processedData.put("Prix Forfait SST", inst.getPrixSst());

        // AX : 0
        processedData.put("Materiel prix", 0.0);

        // AY : =AP2
        processedData.put("MES STT", mes.getCodeForfait());

        // AZ : =RECHERCHEV(AY2,'BPU RACC'!E:F,2,FAUX)
        processedData.put("Prix Forfait MES SST", mes.getPrixSst());

        // BA : =AM2
        processedData.put("Forfait Logistique SST", logEntry.getCodeForfait());

        // BB : =RECHERCHEV(BA2,'BPU RACC'!E:F,2,FAUX)
        processedData.put("Prix Forfait Logistique SST", logEntry.getPrixSst());

        // BC : =BB2+AZ2+AX2+AW2
        double bc = logEntry.getPrixSst() + mes.getPrixSst() + 0.0 + inst.getPrixSst();
        processedData.put("Mt SST", bc);

        // =========================================================
        // Autres colonnes
        // =========================================================
        processedData.put("JARRETIERE_UNI", "-");
        processedData.put("PRIX_HT_JARRETIERE2", 0.0);

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