package com.kyntus.operatingsystem.service.billingengine;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BpuCacheService {

    private final List<BpuEntry> bpuDatabase = new ArrayList<>();

    @PostConstruct
    public void initBpuData() {
        // ==============================================================
        // 📊 BPU RACC (Installation / Déplacement / etc.)
        // ==============================================================
        addBpu(108.0, "E-1.2", 74.0);
        addBpu(67.0, "E-1.1", 40.0);
        addBpu(218.0, "E-1.3", 155.0);
        addBpu(303.0, "E-1.4", 205.0);
        addBpu(273.0, "E-1.5", 182.0);

        addBpu(24.0, "E-2.1", 13.0);
        addBpu(21.0, "E-2.2", 11.0);
        addBpu(20.0, "E-2.3", 10.0);
        addBpu(30.0, "E-2.3.", 16.0);
        addBpu(31.0, "E-2.2.", 17.0);
        addBpu(34.0, "E-2.1.", 19.0);

        addBpu(122.4, "1,8*E-1.1", 81.0);
        addBpu(198.0, "1,8*E-1.2", 145.8);
        addBpu(396.0, "1,8*E-1.3", 304.2);
        addBpu(549.0, "1,8*E-1.4", 396.0);
        addBpu(495.0, "1,8*E-1.5", 345.6);

        addBpu(42.12, "1,8*E-2.1", 27.9);
        addBpu(37.44, "1,8*E-2.2", 24.3);
        addBpu(35.10, "1,8*E-2.3", 22.5);
        addBpu(52.12, "1,8*E-2.1*.", 33.9);
        addBpu(47.44, "1,8*E-2.2*.", 30.3);
        addBpu(45.10, "1,8*E-2.3*.", 28.5);

        addBpu(5.0, "B-1.1", 3.0);
        addBpu(10.0, "B-1.2", 5.0);

        addBpu(78.2, "D-1.1", 40.0);
        addBpu(253.7, "D-1.3", 155.0);
        addBpu(350.75, "D-1.4", 205.0);
        addBpu(316.95, "D-1.5", 182.0);
        addBpu(126.5, "D-1.2", 74.0);

        addBpu(70.0, "E-1.1-S", 40.0);
        addBpu(77.05, "E-1.1-DS", 40.0);
        addBpu(80.5, "E-1.1-S-DS", 40.0);

        addBpu(86.0, "DP-1.1", 55.0);
        addBpu(89.0, "DP-1.1-S", 55.0);
        addBpu(98.9, "DP-1.1-DS", 55.0);
        addBpu(102.35, "DP-1.1-S-DS", 55.0);

        addBpu(221.0, "E-1.3-S", 155.0);
        addBpu(250.7, "E-1.3-DS", 155.0);
        addBpu(254.15, "E-1.3-S-DS", 155.0);

        addBpu(111.0, "E-1.2-S", 74.0);
        addBpu(124.2, "E-1.2-DS", 74.0);
        addBpu(127.2, "E-1.2-S-DS", 74.0);

        addBpu(306.0, "E-1.4-S", 205.0);
        addBpu(348.45, "E-1.4-DS", 205.0);
        addBpu(351.9, "E-1.4-S-DS", 205.0);

        addBpu(276.0, "E-1.5-P", 182.0);
        addBpu(351.45, "E-1.5-S", 182.0);
        addBpu(313.95, "E-1.5-DS", 182.0);
        addBpu(317.4, "E-1.5-S-DS", 182.0);

        addBpu(13.0, "L-1.1", 6.0);
        addBpu(26.0, "L-1.2", 12.0);

        addBpu(48.0, "PLP_ISC", 28.0);
        addBpu(55.2, "PLP_ISC 1,15", 28.0);

        // ==============================================================
        // 📦 EXTRAS (MATERIEL / MES) - Valeurs Gringotts Fallback
        // ==============================================================
        addBpu(41.22, "MAT-41", 30.0);
        addBpu(31.28, "MAT-31", 20.0);
        addBpu(11.03, "MAT-11", 5.0);
        addBpu(28.62, "MAT-28", 15.0);
        addBpu(1.46, "MAT-1", 1.0);

        addBpu(24.0, "MES-24", 15.0);
        addBpu(31.0, "MES-31", 20.0);
        addBpu(34.0, "MES-34", 25.0);
        addBpu(5.0, "MES-5", 3.0);
        addBpu(10.0, "MES-10", 6.0);
    }

    private void addBpu(double kyntusPrice, String code, double sstPrice) {
        bpuDatabase.add(new BpuEntry(code, kyntusPrice, sstPrice));
    }

    // 🔥 THE FIX: L'Algorithme de Recherche Kyntus (Tolerance + Zéro exact)
    public BpuEntry lookupByPrice(double price, String type) {
        // Ila kant 0.0 w kan9elbou 3la Installation awla Support awla Logistique, nreje3 E-0.0
        if (price == 0.0) {
            if (type.equals("INST")) return new BpuEntry("E-0.0", 0.0, 0.0);
            if (type.equals("MAT")) return new BpuEntry("MAT-0.0", 0.0, 0.0);
            if (type.equals("MES")) return new BpuEntry("MES-0.0", 0.0, 0.0);
            return new BpuEntry("E-0.0", 0.0, 0.0); // Par défaut E-0.0 kima bghiti
        }

        for (BpuEntry entry : bpuDatabase) {
            if (Math.abs(entry.getPrixKyntus() - price) < 0.05) {
                return entry;
            }
        }

        // Ila mal9a 7ta haja, yktb l'Prix m3a "N/A" bach t-sahl 3lik t-zidha mnb3d f l'init
        return new BpuEntry("N/A (" + price + ")", price, 0.0);
    }
}