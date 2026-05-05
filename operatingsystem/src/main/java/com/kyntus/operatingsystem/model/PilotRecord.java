package com.kyntus.operatingsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "pilot_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PilotRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "eps_reference")
    private String epsReference;

    // MAGIE HIBERNATE 6 : Katjib JSONB direct l Map f Java
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dynamic_data", columnDefinition = "jsonb")
    private Map<String, Object> dynamicData;

    @Column(name = "version")
    private String version;

    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    @Column(name = "pilot_id")
    private Long pilotId;

    @Column(name = "import_year")
    private Integer importYear;

    @Column(name = "import_month")
    private Integer importMonth;

    @Column(name = "category")
    private String category;

    @Column(name = "source_file")
    private String sourceFile;

    @Column(name = "file_rank")
    private Integer fileRank;
}