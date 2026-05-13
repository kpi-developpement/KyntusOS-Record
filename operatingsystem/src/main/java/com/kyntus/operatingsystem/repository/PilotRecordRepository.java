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

    // Fetch Paginated (L'Affichage Front)
    @Query("SELECT p FROM PilotRecord p WHERE p.category = :category AND p.importYear = :year AND p.importMonth = :month AND UPPER(TRIM(p.version)) = UPPER(TRIM(:version))")
    Page<PilotRecord> findRecordsByCategoryDateAndVersion(@Param("category") String category, @Param("year") int year, @Param("month") int month, @Param("version") String version, Pageable pageable);

    // 🔥 Fetch V1 with Pagination for the Engine
    @Query("SELECT p FROM PilotRecord p WHERE p.category = :category AND p.importYear = :year AND p.importMonth = :month AND (UPPER(TRIM(p.version)) = 'V1' OR p.version IS NULL)")
    Page<PilotRecord> findV1RecordsPageable(@Param("category") String category, @Param("year") int year, @Param("month") int month, Pageable pageable);

    // 🔥 Fetch columns ultra-fast
    @Query(value = "SELECT jsonb_object_keys(dynamic_data) FROM (SELECT dynamic_data FROM pilot_records WHERE category = :category AND import_year = :year AND import_month = :month AND UPPER(TRIM(version)) = UPPER(TRIM(:version)) LIMIT 1) t", nativeQuery = true)
    List<String> findDistinctDynamicColumnsFast(@Param("category") String category, @Param("year") int year, @Param("month") int month, @Param("version") String version);

    // ==========================================================
    // 🔥 ZERO-WRITE NOUVEAU: Fetch V1 Multi-mois M3a Pagination
    // ==========================================================
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

    // THE NUKE (DELETE ALL GHOST V2) - Khellinaha l'Engine l9dim ila knti ba9i baghi tkhdm bih
    @Modifying
    @Transactional
    @Query("DELETE FROM PilotRecord p WHERE p.category = :category AND p.importYear = :year AND p.importMonth = :month AND UPPER(TRIM(p.version)) = 'V2'")
    void deleteOldV2Records(@Param("category") String category, @Param("year") int year, @Param("month") int month);
}