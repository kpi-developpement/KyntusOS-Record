package com.kyntus.operatingsystem.repository;

import com.kyntus.operatingsystem.model.PilotRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PilotRecordRepository extends JpaRepository<PilotRecord, Long> {

    // ==========================================================
    // 🖥️ AFFICHAGE FRONTEND (V2)
    // ==========================================================
    @Query("SELECT p FROM PilotRecord p WHERE p.category = :category AND p.importYear = :year AND p.importMonth = :month AND UPPER(TRIM(p.version)) = UPPER(TRIM(:version))")
    Page<PilotRecord> findRecordsByCategoryDateAndVersion(@Param("category") String category, @Param("year") int year, @Param("month") int month, @Param("version") String version, Pageable pageable);

    @Query(value = "SELECT jsonb_object_keys(dynamic_data) FROM (SELECT dynamic_data FROM pilot_records WHERE category = :category AND import_year = :year AND import_month = :month AND UPPER(TRIM(version)) = UPPER(TRIM(:version)) LIMIT 1) t", nativeQuery = true)
    List<String> findDistinctDynamicColumnsFast(@Param("category") String category, @Param("year") int year, @Param("month") int month, @Param("version") String version);

    // ==========================================================
    // 📺 AFFICHAGE FRONTEND (V1 NOUVEAU - GHIR VALIDATION PARTENAIRE)
    // ==========================================================
    @Query(value = "SELECT * FROM ( " +
            "  SELECT p.*, ROW_NUMBER() OVER (PARTITION BY p.eps_reference ORDER BY p.version ASC) as rn " +
            "  FROM pilot_records p " +
            "  WHERE p.category = :category " +
            "    AND p.import_year = :year " +
            "    AND p.import_month = :month " +
            "    AND (p.source_file IS NULL OR p.source_file != 'KYNTUS_BILLING_ENGINE') " +
            "    AND p.dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%' " +
            ") t WHERE t.rn = 1",
            countQuery = "SELECT COUNT(*) FROM ( " +
                    "  SELECT p.eps_reference " +
                    "  FROM pilot_records p " +
                    "  WHERE p.category = :category " +
                    "    AND p.import_year = :year " +
                    "    AND p.import_month = :month " +
                    "    AND (p.source_file IS NULL OR p.source_file != 'KYNTUS_BILLING_ENGINE') " +
                    "    AND p.dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%' " +
                    "  GROUP BY p.eps_reference " +
                    ") c",
            nativeQuery = true)
    Page<PilotRecord> findV1DisplayPageable(@Param("category") String category, @Param("year") int year, @Param("month") int month, Pageable pageable);

    @Query(value = "SELECT DISTINCT jsonb_object_keys(dynamic_data) FROM pilot_records " +
            "WHERE category = :category " +
            "AND import_year = :year " +
            "AND import_month = :month " +
            "AND (source_file IS NULL OR source_file != 'KYNTUS_BILLING_ENGINE') " +
            "AND dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%'", nativeQuery = true)
    List<String> findDistinctV1DisplayColumnsFast(@Param("category") String category, @Param("year") int year, @Param("month") int month);

    // ==========================================================
    // ⚙️ MOTEUR DE CALCUL (Rje3 kima kan f l'Awal - Original)
    // ==========================================================
    @Query("SELECT p FROM PilotRecord p WHERE p.category = :category AND p.importYear = :year AND p.importMonth = :month AND (UPPER(TRIM(p.version)) = 'V1' OR p.version IS NULL)")
    Page<PilotRecord> findV1RecordsPageable(@Param("category") String category, @Param("year") int year, @Param("month") int month, Pageable pageable);

    @Query("SELECT p FROM PilotRecord p WHERE p.category = :category " +
            "AND (p.importYear * 100 + p.importMonth) >= :startPeriod " +
            "AND (p.importYear * 100 + p.importMonth) <= :endPeriod " +
            "AND (UPPER(TRIM(p.version)) = 'V1' OR p.version IS NULL)")
    Page<PilotRecord> findV1RecordsForExportPageable(
            @Param("category") String category,
            @Param("startPeriod") int startPeriod,
            @Param("endPeriod") int endPeriod,
            Pageable pageable);

    @Query(value = "SELECT DISTINCT jsonb_object_keys(dynamic_data) FROM pilot_records " +
            "WHERE category = :category " +
            "AND (import_year * 100 + import_month) >= :startPeriod " +
            "AND (import_year * 100 + import_month) <= :endPeriod " +
            "AND (UPPER(TRIM(version)) = 'V1' OR version IS NULL)", nativeQuery = true)
    List<String> findDistinctV1ColumnsForExport(
            @Param("category") String category,
            @Param("startPeriod") int startPeriod,
            @Param("endPeriod") int endPeriod);

    @Modifying
    @Transactional
    @Query("DELETE FROM PilotRecord p WHERE p.category = :category AND p.importYear = :year AND p.importMonth = :month AND UPPER(TRIM(p.version)) = 'V2'")
    void deleteOldV2Records(@Param("category") String category, @Param("year") int year, @Param("month") int month);
}