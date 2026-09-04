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

        // 7. 检查并补充 sys_user 表的 is_master 列
        if (!isColumnExist("sys_user", "is_master")) {
            try {
                jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN is_master INT NOT NULL DEFAULT 0");
                log.info("Successfully added is_master column to sys_user");
            } catch (Exception e) {
                log.warn("Failed to add is_master column to sys_user: {}", e.getMessage());
            }
        }

        // 8. 检查并创建 user_sub_account 主-子账号关联表
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS user_sub_account (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "master_user_id BIGINT NOT NULL, " +
                    "sub_user_id BIGINT NOT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE KEY uk_master_sub (master_user_id, sub_user_id), " +
                    "INDEX idx_master_user (master_user_id), " +
                    "INDEX idx_sub_user (sub_user_id)" +
                    ")");
            log.info("Successfully checked/created user_sub_account table");
        } catch (Exception e) {
            log.warn("Failed to create user_sub_account table: {}", e.getMessage());
        }

        // 9. 检查并补充 sys_user 表的 perm_settlement 列与 is_settlement 属性列
        if (!isColumnExist("sys_user", "perm_settlement")) {
            try {
                jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN perm_settlement INT NOT NULL DEFAULT 0");
                log.info("Successfully added perm_settlement column to sys_user");
            } catch (Exception e) {
                log.warn("Failed to add perm_settlement column to sys_user: {}", e.getMessage());
            }
        }
        if (!isColumnExist("sys_user", "perm_video_gen")) {
            try {
                jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN perm_video_gen INT NOT NULL DEFAULT 0");
                log.info("Successfully added perm_video_gen column to sys_user");
            } catch (Exception e) {
                log.warn("Failed to add perm_video_gen column to sys_user: {}", e.getMessage());
            }
        }
        if (!isColumnExist("sys_user", "is_settlement")) {
            try {
                jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN is_settlement INT NOT NULL DEFAULT 0");
                log.info("Successfully added is_settlement column to sys_user");
            } catch (Exception e) {
                log.warn("Failed to add is_settlement column to sys_user: {}", e.getMessage());
            }
        }

        // 10. 检查并创建 monthly_settlement_config 表
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS monthly_settlement_config (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "settlement_type VARCHAR(32) NOT NULL, " +
                    "target_user_id BIGINT DEFAULT NULL, " +
                    "month_str VARCHAR(16) NOT NULL, " +
                    "settled_refund_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00, " +
                    "month_settled_refund_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00, " +
                    "cross_period_refund_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00, " +
                    "share_ratio DECIMAL(6,4) NOT NULL DEFAULT 0.9500, " +
                    "channel_fee_rate DECIMAL(6,4) NOT NULL DEFAULT 0.0700, " +
                    "remark VARCHAR(500) DEFAULT '', " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    "UNIQUE KEY uk_settle_type_user_month (settlement_type, target_user_id, month_str)" +
                    ")");
            log.info("Successfully checked/created monthly_settlement_config table");
        } catch (Exception e) {
            log.warn("Failed to create monthly_settlement_config table: {}", e.getMessage());
        }

        if (!isColumnExist("monthly_settlement_config", "month_settled_refund_amount")) {
            try {
                jdbcTemplate.execute("ALTER TABLE monthly_settlement_config ADD COLUMN month_settled_refund_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00");
                log.info("Successfully added month_settled_refund_amount column to monthly_settlement_config");
            } catch (Exception e) {
                log.warn("Failed to add month_settled_refund_amount column: {}", e.getMessage());
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
