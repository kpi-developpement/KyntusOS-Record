package com.kyntus.operatingsystem.service.billingengine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BpuEntry {
    private String codeForfait; // BPU RACC D:E
    private double prixKyntus;  // BPU RACC C:D
    private double prixSst;     // BPU RACC E:F
}