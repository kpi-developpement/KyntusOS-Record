package com.kyntus.operatingsystem.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initDatabase() {
        log.info("⚙️ [DB INIT] Vérification de l'architecture de la base de données...");
        try {
            // Kat-creeyi la table jdida 'pilot_records_v2' b nafs les colonnes dyal 'pilot_records'
            // 'IF NOT EXISTS' bach ma-t-plantich ila kanet dejà kayna (f les prochains redémarrages)
            String sql = "CREATE TABLE IF NOT EXISTS pilot_records_v2 (LIKE pilot_records INCLUDING ALL)";
            jdbcTemplate.execute(sql);
            log.info("✅ [DB INIT] Table d'isolation 'pilot_records_v2' vérifiée/créée avec succès !");
        } catch (Exception e) {
            log.error("❌ [DB INIT] Erreur lors de la création de la table 'pilot_records_v2': ", e);
        }
    }
}