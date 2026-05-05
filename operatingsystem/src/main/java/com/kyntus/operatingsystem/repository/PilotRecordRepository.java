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

    // Fetch Paginated
    @Query("SELECT p FROM PilotRecord p WHERE p.category = :category AND p.importYear = :year AND p.importMonth = :month AND UPPER(TRIM(p.version)) = UPPER(TRIM(:version))")
    Page<PilotRecord> findRecordsByCategoryDateAndVersion(@Param("category") String category, @Param("year") int year, @Param("month") int month, @Param("version") String version, Pageable pageable);

    // Fetch V1 for Engine
    @Query("SELECT p FROM PilotRecord p WHERE p.category = :category AND p.importYear = :year AND p.importMonth = :month AND (UPPER(TRIM(p.version)) = 'V1' OR p.version IS NULL)")
    List<PilotRecord> findAllV1RecordsByCategoryAndDate(@Param("category") String category, @Param("year") int year, @Param("month") int month);

    // Fetch columns
    @Query(value = "SELECT DISTINCT jsonb_object_keys(dynamic_data) FROM pilot_records WHERE category = :category AND import_year = :year AND import_month = :month AND UPPER(TRIM(version)) = UPPER(TRIM(:version))", nativeQuery = true)
    List<String> findDistinctDynamicColumns(@Param("category") String category, @Param("year") int year, @Param("month") int month, @Param("version") String version);

    // 🔥 THE NUKE (DELETE ALL GHOST V2) 🔥
    @Modifying
    @Transactional
    @Query("DELETE FROM PilotRecord p WHERE p.category = :category AND p.importYear = :year AND p.importMonth = :month AND UPPER(TRIM(p.version)) = 'V2'")
    void deleteOldV2Records(@Param("category") String category, @Param("year") int year, @Param("month") int month);
}