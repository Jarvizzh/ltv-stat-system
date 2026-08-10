package com.ltv.stat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DatabasePrimaryKeysInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabasePrimaryKeysInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabasePrimaryKeysInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void fixPrimaryKeys() {
        // 1. 检查并修正 ltv_daily_stat 主键为 (user_id, launch_date)
        ensurePrimaryKey("ltv_daily_stat", "user_id,launch_date",
                "ALTER TABLE ltv_daily_stat DROP PRIMARY KEY, ADD PRIMARY KEY (user_id, launch_date)");

        // 2. 检查并修正 daily_recharge_distribution 主键为 (user_id, date)
        ensurePrimaryKey("daily_recharge_distribution", "user_id,date",
                "ALTER TABLE daily_recharge_distribution DROP PRIMARY KEY, ADD PRIMARY KEY (user_id, date)");

        // 3. 检查并修正 ltv_launch_config 主键为 (user_id, launch_date)
        ensurePrimaryKey("ltv_launch_config", "user_id,launch_date",
                "ALTER TABLE ltv_launch_config DROP PRIMARY KEY, ADD PRIMARY KEY (user_id, launch_date)");

        // 4. 检查并补充 day31_recharge ~ day60_recharge & day31_roi ~ day60_roi 列
        boolean needAddColumns = false;
        for (int d = 31; d <= 60; d++) {
            String rechargeCol = "day" + d + "_recharge";
            String roiCol = "day" + d + "_roi";

            if (!isColumnExist("ltv_daily_stat", rechargeCol)) {
                try {
                    jdbcTemplate.execute("ALTER TABLE ltv_daily_stat ADD COLUMN " + rechargeCol + " DECIMAL(10,2) DEFAULT 0.00");
                    needAddColumns = true;
                } catch (Exception ignored) {}
            }
            if (!isColumnExist("ltv_daily_stat", roiCol)) {
                try {
                    jdbcTemplate.execute("ALTER TABLE ltv_daily_stat ADD COLUMN " + roiCol + " DECIMAL(10,4) DEFAULT 0.0000");
                    needAddColumns = true;
                } catch (Exception ignored) {}
            }
        }
        if (needAddColumns) {
            log.info("Successfully added missing day31 ~ day60 columns to ltv_daily_stat");
        } else {
            log.debug("ltv_daily_stat day31 ~ day60 columns already up to date.");
        }

        // 5. 检查并补充 raw_order 表的 refund_status 列
        if (!isColumnExist("raw_order", "refund_status")) {
            try {
                jdbcTemplate.execute("ALTER TABLE raw_order ADD COLUMN refund_status INT DEFAULT 0");
                log.info("Successfully added refund_status column to raw_order");
            } catch (Exception e) {
                log.warn("Failed to add refund_status column to raw_order: {}", e.getMessage());
            }
        }

        // 6. 检查并补充 ltv_daily_stat 表的 total_refund 列
        if (!isColumnExist("ltv_daily_stat", "total_refund")) {
            try {
                jdbcTemplate.execute("ALTER TABLE ltv_daily_stat ADD COLUMN total_refund DECIMAL(10, 2) NOT NULL DEFAULT 0.00 AFTER total_recharge");
                log.info("Successfully added total_refund column to ltv_daily_stat");
            } catch (Exception e) {
                log.warn("Failed to add total_refund column to ltv_daily_stat: {}", e.getMessage());
            }
        }
    }

    private void ensurePrimaryKey(String tableName, String expectedCols, String alterSql) {
        try {
            String currentPk = getPrimaryKeyColumns(tableName);
            if (expectedCols.equalsIgnoreCase(currentPk)) {
                log.debug("Table {} primary key is already ({}), skipping ALTER.", tableName, currentPk);
                return;
            }
            jdbcTemplate.execute(alterSql);
            log.info("Successfully updated {} PRIMARY KEY to ({})", tableName, expectedCols);
        } catch (Exception e) {
            log.info("{} PRIMARY KEY check failed: {}", tableName, e.getMessage());
        }
    }

    private String getPrimaryKeyColumns(String tableName) {
        try {
            String sql = "SELECT GROUP_CONCAT(column_name ORDER BY seq_in_index) " +
                         "FROM information_schema.statistics " +
                         "WHERE table_schema = DATABASE() AND table_name = ? AND index_name = 'PRIMARY'";
            return jdbcTemplate.queryForObject(sql, String.class, tableName);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isColumnExist(String tableName, String columnName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.columns " +
                         "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName, columnName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
