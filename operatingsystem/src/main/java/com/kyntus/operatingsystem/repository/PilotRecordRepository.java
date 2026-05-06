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

    // 🔥 THE TURBO FIX 1: Fetch V1 with Pagination for the Engine (10k by 10k)
    @Query("SELECT p FROM PilotRecord p WHERE p.category = :category AND p.importYear = :year AND p.importMonth = :month AND (UPPER(TRIM(p.version)) = 'V1' OR p.version IS NULL)")
    Page<PilotRecord> findV1RecordsPageable(@Param("category") String category, @Param("year") int year, @Param("month") int month, Pageable pageable);

    // 🔥 THE TURBO FIX 2: Fetch columns ultra-fast (Reads only 1 row instead of 34,000!)
    @Query(value = "SELECT jsonb_object_keys(dynamic_data) FROM (SELECT dynamic_data FROM pilot_records WHERE category = :category AND import_year = :year AND import_month = :month AND UPPER(TRIM(version)) = UPPER(TRIM(:version)) LIMIT 1) t", nativeQuery = true)
    List<String> findDistinctDynamicColumnsFast(@Param("category") String category, @Param("year") int year, @Param("month") int month, @Param("version") String version);

    // THE NUKE (DELETE ALL GHOST V2)
    @Modifying
    @Transactional
    @Query("DELETE FROM PilotRecord p WHERE p.category = :category AND p.importYear = :year AND p.importMonth = :month AND UPPER(TRIM(p.version)) = 'V2'")
    void deleteOldV2Records(@Param("category") String category, @Param("year") int year, @Param("month") int month);
}