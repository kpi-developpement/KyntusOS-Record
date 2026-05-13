package com.kyntus.operatingsystem.controller.record;

import com.kyntus.operatingsystem.model.PilotRecord;
import com.kyntus.operatingsystem.service.record.RecordV1Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/os/records/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecordV1Controller {

    private final RecordV1Service recordV1Service;

    @GetMapping("/columns")
    public ResponseEntity<List<String>> getColumns(
            @RequestParam String category,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "V2") String version) {
        return ResponseEntity.ok(recordV1Service.getAvailableColumns(category, year, month, version));
    }

    @GetMapping("/data")
    public ResponseEntity<Page<PilotRecord>> getRecordsData(
            @RequestParam String category,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) List<String> columns,
            @RequestParam(defaultValue = "V2") String version) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PilotRecord> data = recordV1Service.getV1RecordsFiltered(category, year, month, pageable, columns, version);
        return ResponseEntity.ok(data);
    }

    // ==========================================================
    // 🔥 ENDPOINT: ZERO-WRITE EXPORT
    // ==========================================================
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportData(
            @RequestParam String category,
            @RequestParam int startYear,
            @RequestParam int startMonth,
            @RequestParam int endYear,
            @RequestParam int endMonth,
            @RequestParam(required = false) String version, // Khlinahoum bach l'Frontend may-drbch erreur
            @RequestParam(required = false) Boolean autoCalculate) {

        // 🚀 L'APPEL L'ON-THE-FLY EXPORT!
        byte[] csvData = recordV1Service.generateCsvExportOnTheFly(category, startYear, startMonth, endYear, endMonth);

        String filename = "Kyntus_Export_" + category + "_V2_" + startYear + "-" + startMonth + "_au_" + endYear + "-" + endMonth + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
                .body(csvData);
    }
}