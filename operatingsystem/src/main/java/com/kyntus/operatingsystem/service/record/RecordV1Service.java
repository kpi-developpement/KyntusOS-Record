package com.kyntus.operatingsystem.service.record;

import com.kyntus.operatingsystem.model.PilotRecord;
import com.kyntus.operatingsystem.repository.PilotRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordV1Service {

    private final PilotRecordRepository repository;

    public List<String> getAvailableColumns(String category, int year, int month, String version) {
        return repository.findDistinctDynamicColumns(category, year, month, version);
    }

    public Page<PilotRecord> getV1RecordsFiltered(String category, int year, int month, Pageable pageable, List<String> selectedColumns, String version) {
        // 🔥 LE FIX RADICAL :
        // 7iydna l'filtre b `containsKey` 7it l'espaces f l'Excel kano kay-khel9o décalage.
        // Daba l'Backend ghay-sifet l'JSON kaml kima t-calcula, w l'Frontend ghay-tkllef y-affichih.
        return repository.findRecordsByCategoryDateAndVersion(category, year, month, version, pageable);
    }
}