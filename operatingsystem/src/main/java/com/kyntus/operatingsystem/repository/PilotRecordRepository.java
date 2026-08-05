package com.kyntus.operatingsystem.repository;

import com.kyntus.operatingsystem.model.PilotRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PilotRecordRepository extends JpaRepository<PilotRecord, Long> {

    // ==========================================================
    // 🖥️ AFFICHAGE FRONTEND (Supporte les 2 tables : V1 et V2)
    // ==========================================================
    @Query(value = "SELECT * FROM (SELECT * FROM pilot_records UNION ALL SELECT * FROM pilot_records_v2) t WHERE category = :category AND import_year = :year AND import_month = :month AND UPPER(TRIM(version)) = UPPER(TRIM(:version))",
            countQuery = "SELECT COUNT(*) FROM (SELECT * FROM pilot_records UNION ALL SELECT * FROM pilot_records_v2) t WHERE category = :category AND import_year = :year AND import_month = :month AND UPPER(TRIM(version)) = UPPER(TRIM(:version))",
            nativeQuery = true)
    Page<PilotRecord> findRecordsByCategoryDateAndVersion(@Param("category") String category, @Param("year") int year, @Param("month") int month, @Param("version") String version, Pageable pageable);

    @Query(value = "SELECT jsonb_object_keys(dynamic_data) FROM (SELECT dynamic_data FROM (SELECT * FROM pilot_records UNION ALL SELECT * FROM pilot_records_v2) tbl WHERE category = :category AND import_year = :year AND import_month = :month AND UPPER(TRIM(version)) = UPPER(TRIM(:version)) LIMIT 1) t", nativeQuery = true)
    List<String> findDistinctDynamicColumnsFast(@Param("category") String category, @Param("year") int year, @Param("month") int month, @Param("version") String version);

    // ==========================================================
    // 🔥 L'ALGORITHME DE FALLBACK : CHERCHE V1 SINON LA DERNIÈRE VERSION "EN ATTENTE"
    // ==========================================================
    @Query(value = "SELECT * FROM ( " +
            "  SELECT p.*, ROW_NUMBER() OVER ( " +
            "      PARTITION BY p.eps_reference " +
            "      ORDER BY " +
            "          CASE WHEN UPPER(TRIM(p.version)) = 'V1' THEN 1 ELSE 2 END ASC, " +
            "          p.version DESC " +
            "  ) as rn " +
            "  FROM pilot_records p " +
            "  WHERE p.category = :category " +
            "    AND p.import_year = :year " +
            "    AND p.import_month = :month " +
            "    AND p.dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%' " +
            "    AND (p.source_file IS NULL OR p.source_file != 'KYNTUS_BILLING_ENGINE') " +
            ") t WHERE t.rn = 1 ORDER BY t.id ASC",
            countQuery = "SELECT COUNT(DISTINCT p.eps_reference) FROM pilot_records p " +
                    "WHERE p.category = :category " +
                    "  AND p.import_year = :year " +
                    "  AND p.import_month = :month " +
                    "  AND p.dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%' " +
                    "  AND (p.source_file IS NULL OR p.source_file != 'KYNTUS_BILLING_ENGINE')",
            nativeQuery = true)
    Page<PilotRecord> findV1RecordsPageable(@Param("category") String category, @Param("year") int year, @Param("month") int month, Pageable pageable);

    @Query(value = "SELECT DISTINCT jsonb_object_keys(dynamic_data) FROM pilot_records " +
            "WHERE category = :category " +
            "AND import_year = :year " +
            "AND import_month = :month " +
            "AND dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%'", nativeQuery = true)
    List<String> findDistinctV1ColumnsFast(@Param("category") String category, @Param("year") int year, @Param("month") int month);

    // ==========================================================
    // 🔥 ZERO-WRITE EXPORT: AVEC LE MÊME ALGORITHME DE FALLBACK
    // ==========================================================
    @Query(value = "SELECT * FROM ( " +
            "  SELECT p.*, ROW_NUMBER() OVER ( " +
            "      PARTITION BY p.eps_reference " +
            "      ORDER BY " +
            "          CASE WHEN UPPER(TRIM(p.version)) = 'V1' THEN 1 ELSE 2 END ASC, " +
            "          p.version DESC " +
            "  ) as rn " +
            "  FROM pilot_records p " +
            "  WHERE p.category = :category " +
            "    AND (p.import_year * 100 + p.import_month) >= :startPeriod " +
            "    AND (p.import_year * 100 + p.import_month) <= :endPeriod " +
            "    AND p.dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%' " +
            "    AND (p.source_file IS NULL OR p.source_file != 'KYNTUS_BILLING_ENGINE') " +
            ") t WHERE t.rn = 1 ORDER BY t.id ASC",
            countQuery = "SELECT COUNT(DISTINCT p.eps_reference) FROM pilot_records p " +
                    "WHERE p.category = :category " +
                    "  AND (p.import_year * 100 + p.import_month) >= :startPeriod " +
                    "  AND (p.import_year * 100 + p.import_month) <= :endPeriod " +
                    "  AND p.dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%' " +
                    "  AND (p.source_file IS NULL OR p.source_file != 'KYNTUS_BILLING_ENGINE')",
            nativeQuery = true)
    Page<PilotRecord> findV1RecordsForExportPageable(
            @Param("category") String category,
            @Param("startPeriod") int startPeriod,
            @Param("endPeriod") int endPeriod,
            Pageable pageable);

    @Query(value = "SELECT DISTINCT jsonb_object_keys(dynamic_data) FROM pilot_records " +
            "WHERE category = :category " +
            "AND (import_year * 100 + import_month) >= :startPeriod " +
            "AND (import_year * 100 + import_month) <= :endPeriod " +
            "AND dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%'", nativeQuery = true)
    List<String> findDistinctV1ColumnsForExport(
            @Param("category") String category,
            @Param("startPeriod") int startPeriod,
            @Param("endPeriod") int endPeriod);
}