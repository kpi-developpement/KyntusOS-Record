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
    // 🔥 THE GRANDMASTER FETCH: CLEAN V1 FETCH FOR BILLING ENGINE
    // ==========================================================
    @Query(value = "SELECT * FROM pilot_records " +
            "WHERE category = :category " +
            "  AND import_year = :year " +
            "  AND import_month = :month " +
            "  AND UPPER(TRIM(version)) = 'V1' " +
            "  AND dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%' " +
            "ORDER BY id ASC",
            countQuery = "SELECT COUNT(*) FROM pilot_records " +
                    "WHERE category = :category " +
                    "  AND import_year = :year " +
                    "  AND import_month = :month " +
                    "  AND UPPER(TRIM(version)) = 'V1' " +
                    "  AND dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%'",
            nativeQuery = true)
    Page<PilotRecord> findV1RecordsPageable(@Param("category") String category, @Param("year") int year, @Param("month") int month, Pageable pageable);

    @Query(value = "SELECT DISTINCT jsonb_object_keys(dynamic_data) FROM pilot_records " +
            "WHERE category = :category " +
            "AND import_year = :year " +
            "AND import_month = :month " +
            "AND UPPER(TRIM(version)) = 'V1' " +
            "AND dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%'", nativeQuery = true)
    List<String> findDistinctV1ColumnsFast(@Param("category") String category, @Param("year") int year, @Param("month") int month);

    // ==========================================================
    // 🔥 ZERO-WRITE EXPORT: CLEAN V1 FETCH
    // ==========================================================
    @Query(value = "SELECT * FROM pilot_records " +
            "WHERE category = :category " +
            "  AND (import_year * 100 + import_month) >= :startPeriod " +
            "  AND (import_year * 100 + import_month) <= :endPeriod " +
            "  AND UPPER(TRIM(version)) = 'V1' " +
            "  AND dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%' " +
            "ORDER BY id ASC",
            countQuery = "SELECT COUNT(*) FROM pilot_records " +
                    "WHERE category = :category " +
                    "  AND (import_year * 100 + import_month) >= :startPeriod " +
                    "  AND (import_year * 100 + import_month) <= :endPeriod " +
                    "  AND UPPER(TRIM(version)) = 'V1' " +
                    "  AND dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%'",
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
            "AND UPPER(TRIM(version)) = 'V1' " +
            "AND dynamic_data->>'etat' ILIKE '%EN_ATTENTE_VALIDATION_PARTENAIRE%'", nativeQuery = true)
    List<String> findDistinctV1ColumnsForExport(
            @Param("category") String category,
            @Param("startPeriod") int startPeriod,
            @Param("endPeriod") int endPeriod);

    // ==========================================================
    // 🧹 THE NUKE (DELETE ALL V2 - FORCED NATIVE DB CLEAR)
    // ==========================================================
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "DELETE FROM pilot_records WHERE category = :category AND import_year = :year AND import_month = :month AND UPPER(TRIM(version)) = 'V2'", nativeQuery = true)
    void deleteOldV2Records(@Param("category") String category, @Param("year") int year, @Param("month") int month);
}