package com.training.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaUpgradeRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SchemaUpgradeRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public SchemaUpgradeRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            ensureColumn(
                    "train_project_publish",
                    "process_weight",
                    "DECIMAL(5,4) NULL COMMENT '过程得分权重(0-1)' AFTER assessment_standard"
            );
            ensureColumn(
                    "train_project_publish",
                    "team_weight",
                    "DECIMAL(5,4) NULL COMMENT '团队协作得分权重(0-1)' AFTER process_weight"
            );
            ensureColumn(
                    "train_project_publish",
                    "final_weight",
                    "DECIMAL(5,4) NULL COMMENT '答辩得分权重(0-1)' AFTER team_weight"
            );

            jdbcTemplate.update("UPDATE train_project_publish " +
                    "SET process_weight = COALESCE(process_weight, 0.4000), " +
                    "team_weight = COALESCE(team_weight, 0.3000), " +
                    "final_weight = COALESCE(final_weight, 0.3000) " +
                    "WHERE is_deleted = 0");
        } catch (Exception ex) {
            log.warn("自动升级开设计划权重字段失败，将继续使用当前数据库结构: {}", ex.getMessage());
        }
    }

    private void ensureColumn(String tableName, String columnName, String definitionSql) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class,
                tableName,
                columnName
        );
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definitionSql);
            log.info("已自动补齐字段 {}.{}", tableName, columnName);
        }
    }
}
