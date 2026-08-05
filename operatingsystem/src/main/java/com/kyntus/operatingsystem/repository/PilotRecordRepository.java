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
    // 🖥️ AFFICHAGE FRONTEND (V2) - LECTURE EXCLUSIVE DE APP 2 (ISOLÉE)
    // ==========================================================
    // Ici on lit UNIQUEMENT pilot_records_v2. On ignore complètement les V2 manuelles de App 1 !
    @Query(value = "SELECT * FROM pilot_records_v2 WHERE category = :category AND import_year = :year AND import_month = :month AND UPPER(TRIM(version)) = UPPER(TRIM(:version))",
            countQuery = "SELECT COUNT(*) FROM pilot_records_v2 WHERE category = :category AND import_year = :year AND import_month = :month AND UPPER(TRIM(version)) = UPPER(TRIM(:version))",
            nativeQuery = true)
    Page<PilotRecord> findRecordsByCategoryDateAndVersion(@Param("category") String category, @Param("year") int year, @Param("month") int month, @Param("version") String version, Pageable pageable);

    @Query(value = "SELECT jsonb_object_keys(dynamic_data) FROM pilot_records_v2 WHERE category = :category AND import_year = :year AND import_month = :month AND UPPER(TRIM(version)) = UPPER(TRIM(:version)) LIMIT 1", nativeQuery = true)
    List<String> findDistinctDynamicColumnsFast(@Param("category") String category, @Param("year") int year, @Param("month") int month, @Param("version") String version);

    // ==========================================================
    // 🔥 L'ALGORITHME DE FALLBACK : LIT DEPUIS APP 1 (pilot_records)
    // ==========================================================
    // Cherche V1, sinon la dernière version (V4, V9, etc) ayant "EN_ATTENTE_VALIDATION_PARTENAIRE"
    @Query(value = "SELECT * FROM ( " +
            "  SELECT p.*, ROW_NUMBER() OVER ( " +
            "      PARTITION BY p.eps_reference " +
            "      ORDER BY " +
            "          CASE WHEN UPPER(TRIM(p.version)) = 'V1' THEN 1 ELSE 2 END ASC, " +
            "          p.version DESC, " +
            "          p.id ASC " +
            "  ) as rn " +
            "  FROM pilot_records p " +
            "  WHERE p.category = :category " +
            "    AND p.import_year = :year " +
            "    AND p.import_month = :month " +
            "    AND p.dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%' " +
            ") t WHERE t.rn = 1 ORDER BY t.id ASC",
            countQuery = "SELECT COUNT(DISTINCT p.eps_reference) FROM pilot_records p " +
                    "WHERE p.category = :category " +
                    "  AND p.import_year = :year " +
                    "  AND p.import_month = :month " +
                    "  AND p.dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%' ",
            nativeQuery = true)
    Page<PilotRecord> findV1RecordsPageable(@Param("category") String category, @Param("year") int year, @Param("month") int month, Pageable pageable);

    @Query(value = "SELECT DISTINCT jsonb_object_keys(dynamic_data) FROM pilot_records " +
            "WHERE category = :category " +
            "AND import_year = :year " +
            "AND import_month = :month " +
            "AND dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%'", nativeQuery = true)
    List<String> findDistinctV1ColumnsFast(@Param("category") String category, @Param("year") int year, @Param("month") int month);

    // ==========================================================
    // 🔥 ZERO-WRITE EXPORT: AVEC LE MÊME ALGORITHME DE FALLBACK (APP 1)
    // ==========================================================
    @Query(value = "SELECT * FROM ( " +
            "  SELECT p.*, ROW_NUMBER() OVER ( " +
            "      PARTITION BY p.eps_reference " +
            "      ORDER BY " +
            "          CASE WHEN UPPER(TRIM(p.version)) = 'V1' THEN 1 ELSE 2 END ASC, " +
            "          p.version DESC, " +
            "          p.id ASC " +
            "  ) as rn " +
            "  FROM pilot_records p " +
            "  WHERE p.category = :category " +
            "    AND (p.import_year * 100 + p.import_month) >= :startPeriod " +
            "    AND (p.import_year * 100 + p.import_month) <= :endPeriod " +
            "    AND p.dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%' " +
            ") t WHERE t.rn = 1 ORDER BY t.id ASC",
            countQuery = "SELECT COUNT(DISTINCT p.eps_reference) FROM pilot_records p " +
                    "WHERE p.category = :category " +
                    "  AND (p.import_year * 100 + p.import_month) >= :startPeriod " +
                    "  AND (p.import_year * 100 + p.import_month) <= :endPeriod " +
                    "  AND p.dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%' ",
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