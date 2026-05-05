package com.kyntus.operatingsystem.service.billingengine.sav;

import com.kyntus.operatingsystem.service.billingengine.BpuEntry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SavBpuCacheService {

    private final List<BpuEntry> bpuDatabase = new ArrayList<>();

    @PostConstruct
    public void initBpuData() {
        // ==============================================================
        // 📊 BPU SAV (Service Après Vente)
        // addBpu(Prix_Kyntus, "Code_Forfait", Prix_SST);
        // ==============================================================

        addBpu(0.0, "SAV-0.0", 0.0);
        addBpu(68.0, "SAV-1.1", 40.0);
        addBpu(99.0, "SAV-1.2", 55.0);
        addBpu(180.0, "SAV-1.3", 90.0);
        addBpu(218.0, "SAV-1.4", 110.0);
        addBpu(250.0, "SAV-1.5", 135.0);

        addBpu(49.0, "DEP", 24.0);

        addBpu(0.0, "EXP-0.0", 0.0);
        addBpu(92.0, "EXP-1.1", 0.0); // SST est vide/null f l'Excel donc 0.0

        addBpu(0.0, "DEFALQUE  SAV-0.0", 0.0);

        addBpu(13.6, "20%SAV-1.1", 8.0);
        addBpu(22.6, "20%SAV-1.1 NAC", 8.0);
        addBpu(19.8, "20%SAV-1.2", 11.0);
        addBpu(36.0, "20%SAV-1.3", 18.0);
        addBpu(43.6, "20%SAV-1.4", 22.0);
        addBpu(50.0, "20%SAV-1.5", 27.0);

        addBpu(10.0, "20%SAV SC-1.1", 3.2);
        addBpu(15.0, "20%SAV GTI-1.1", 8.6);
        addBpu(9.8, "20% DEP", 4.8);

        addBpu(68.0, "SAV MC-0.0", 40.0);
        addBpu(110.0, "SAV MC-1.1", 62.0);
        addBpu(180.0, "SAV MC-1.2", 100.0);
        addBpu(220.0, "SAV MC-1.3", 181.0);
        addBpu(580.0, "SAV MC-1.4", 261.0);
        addBpu(840.0, "SAV MC-1.5", 399.0);

        addBpu(0.0, "SAV GTI-0.0", 0.0);
        addBpu(75.0, "SAV GTI-1.1", 43.0);
        addBpu(99.0, "SAV GTI-1.2", 52.0);

        addBpu(50.0, "INST SC-1.1", 35.0);
        addBpu(6.0, "20% INST SC-1.3", 4.0);
        addBpu(4.0, "20% INST SC-1.2", 2.4);

        addBpu(30.0, "SAV SC-1.1", 16.0);
        addBpu(20.0, "SAV SC-1.2", 12.0);

        addBpu(45.0, "NAC 1.1", 35.0);
        addBpu(34.0, "SAV-GRP", 20.0);
        addBpu(6.8, "20% SAV-GRP", 4.0);
    }

    private void addBpu(double kyntusPrice, String code, double sstPrice) {
        bpuDatabase.add(new BpuEntry(code, kyntusPrice, sstPrice));
    }

    public BpuEntry lookupByPrice(double price) {
        if (price == 0.0) return new BpuEntry("SAV-0.0", 0.0, 0.0);

        for (BpuEntry entry : bpuDatabase) {
            if (Math.abs(entry.getPrixKyntus() - price) < 0.05) {
                return entry;
            }
        }

        return new BpuEntry("N/A (" + price + ")", price, 0.0);
    }
}