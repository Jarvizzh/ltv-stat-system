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
        // 1. 检查并补充 day31_recharge ~ day60_recharge & day31_roi ~ day60_roi 列
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

        // 11. 执行多平台拓展架构自动平滑迁移
        migrateMultiPlatformSchema();
    }

    private void migrateMultiPlatformSchema() {
        // 1. 创建 platform_config 表并插入初始枚举平台
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS platform_config (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "platform_code VARCHAR(32) NOT NULL UNIQUE, " +
                    "platform_name VARCHAR(64) NOT NULL, " +
                    "auth_type VARCHAR(32) NOT NULL DEFAULT 'TOKEN_COOKIE', " +
                    "auth_credentials TEXT, " +
                    "sync_cron VARCHAR(32) DEFAULT '0 5 * * * ?', " +
                    "launch_start_date DATE DEFAULT NULL, " +
                    "status INT NOT NULL DEFAULT 1, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");

            if (!isColumnExist("platform_config", "launch_start_date")) {
                jdbcTemplate.execute("ALTER TABLE platform_config ADD COLUMN launch_start_date DATE DEFAULT NULL AFTER sync_cron");
            }

            jdbcTemplate.execute("INSERT INTO platform_config (platform_code, platform_name, auth_type, launch_start_date, status) " +
                    "VALUES ('rocnovel', '中文在线', 'TOKEN_COOKIE', '2026-07-10', 1) " +
                    "ON DUPLICATE KEY UPDATE platform_name = VALUES(platform_name), launch_start_date = VALUES(launch_start_date)");
            jdbcTemplate.execute("INSERT INTO platform_config (platform_code, platform_name, auth_type, launch_start_date, status, auth_credentials) " +
                    "VALUES ('flicknovel', '番茄司南', 'ED25519_KEY', '2026-09-17', 1, '{\"companyId\":\"355549587538358272\",\"privateKey\":\"ymcPnTqpiQOAtROHJoeegoovJxS7wv6t0HLDUv5q3/G4qry6yKcvjYwhrBqwuEIMjfXMIIqDe0YUPu9JaPofMQ==\"}') " +
                    "ON DUPLICATE KEY UPDATE platform_name = VALUES(platform_name), auth_type = 'ED25519_KEY', launch_start_date = VALUES(launch_start_date)");
            jdbcTemplate.execute("UPDATE platform_config SET platform_name = '番茄司南' WHERE platform_code = 'flicknovel'");
            log.info("Checked/initialized platform_config table with default platforms and launch_start_date");
        } catch (Exception e) {
            log.warn("Failed to create/init platform_config table: {}", e.getMessage());
        }

        // 2. 检查并补充 raw_order 表的 platform_code 和 raw_payload
        if (!isColumnExist("raw_order", "platform_code")) {
            try {
                jdbcTemplate.execute("ALTER TABLE raw_order ADD COLUMN platform_code VARCHAR(32) NOT NULL DEFAULT 'rocnovel' AFTER id");
                log.info("Successfully added platform_code to raw_order");
            } catch (Exception e) {
                log.warn("Failed to add platform_code to raw_order: {}", e.getMessage());
            }
        }
        if (!isColumnExist("raw_order", "raw_payload")) {
            try {
                jdbcTemplate.execute("ALTER TABLE raw_order ADD COLUMN raw_payload TEXT DEFAULT NULL");
                log.info("Successfully added raw_payload to raw_order");
            } catch (Exception e) {
                log.warn("Failed to add raw_payload to raw_order: {}", e.getMessage());
            }
        }
        try {
            if (isIndexExist("raw_order", "order_id")) {
                jdbcTemplate.execute("ALTER TABLE raw_order DROP INDEX order_id");
            }
            if (!isIndexExist("raw_order", "uk_platform_order")) {
                jdbcTemplate.execute("ALTER TABLE raw_order ADD UNIQUE KEY uk_platform_order (platform_code, order_id)");
                log.info("Successfully updated raw_order unique key to (platform_code, order_id)");
            }
        } catch (Exception e) {
            log.info("raw_order unique key update info: {}", e.getMessage());
        }

        // 3. 检查并补充 sys_user 表的 allowed_platforms 列
        if (!isColumnExist("sys_user", "allowed_platforms")) {
            try {
                jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN allowed_platforms VARCHAR(255) NOT NULL DEFAULT 'ALL'");
                log.info("Successfully added allowed_platforms to sys_user");
            } catch (Exception e) {
                log.warn("Failed to add allowed_platforms to sys_user: {}", e.getMessage());
            }
        }

        // 4. 检查并补充 ltv_launch_config 表的 platform_code 并修正复合主键
        if (!isColumnExist("ltv_launch_config", "platform_code")) {
            try {
                jdbcTemplate.execute("ALTER TABLE ltv_launch_config ADD COLUMN platform_code VARCHAR(32) NOT NULL DEFAULT 'rocnovel'");
                log.info("Successfully added platform_code to ltv_launch_config");
            } catch (Exception e) {
                log.warn("Failed to add platform_code to ltv_launch_config: {}", e.getMessage());
            }
        }
        ensurePrimaryKey("ltv_launch_config", "platform_code,user_id,launch_date",
                "ALTER TABLE ltv_launch_config DROP PRIMARY KEY, ADD PRIMARY KEY (platform_code, user_id, launch_date)");

        // 5. 检查并补充 ltv_daily_stat 表的 platform_code 并修正复合主键
        if (!isColumnExist("ltv_daily_stat", "platform_code")) {
            try {
                jdbcTemplate.execute("ALTER TABLE ltv_daily_stat ADD COLUMN platform_code VARCHAR(32) NOT NULL DEFAULT 'rocnovel'");
                log.info("Successfully added platform_code to ltv_daily_stat");
            } catch (Exception e) {
                log.warn("Failed to add platform_code to ltv_daily_stat: {}", e.getMessage());
            }
        }
        ensurePrimaryKey("ltv_daily_stat", "platform_code,user_id,launch_date",
                "ALTER TABLE ltv_daily_stat DROP PRIMARY KEY, ADD PRIMARY KEY (platform_code, user_id, launch_date)");

        // 6. 检查并补充 daily_recharge_distribution 表的 platform_code 并修正复合主键
        if (!isColumnExist("daily_recharge_distribution", "platform_code")) {
            try {
                jdbcTemplate.execute("ALTER TABLE daily_recharge_distribution ADD COLUMN platform_code VARCHAR(32) NOT NULL DEFAULT 'rocnovel'");
                log.info("Successfully added platform_code to daily_recharge_distribution");
            } catch (Exception e) {
                log.warn("Failed to add platform_code to daily_recharge_distribution: {}", e.getMessage());
            }
        }
        ensurePrimaryKey("daily_recharge_distribution", "platform_code,user_id,date",
                "ALTER TABLE daily_recharge_distribution DROP PRIMARY KEY, ADD PRIMARY KEY (platform_code, user_id, date)");

        // 7. 检查并补充 user_landing_page 表的 platform_code
        if (!isColumnExist("user_landing_page", "platform_code")) {
            try {
                jdbcTemplate.execute("ALTER TABLE user_landing_page ADD COLUMN platform_code VARCHAR(32) NOT NULL DEFAULT 'rocnovel'");
                log.info("Successfully added platform_code to user_landing_page");
            } catch (Exception e) {
                log.warn("Failed to add platform_code to user_landing_page: {}", e.getMessage());
            }
        }

        // 8. 检查并补充 subscription_config_version 表的 platform_code
        if (!isColumnExist("subscription_config_version", "platform_code")) {
            try {
                jdbcTemplate.execute("ALTER TABLE subscription_config_version ADD COLUMN platform_code VARCHAR(32) NOT NULL DEFAULT 'rocnovel'");
                log.info("Successfully added platform_code to subscription_config_version");
            } catch (Exception e) {
                log.warn("Failed to add platform_code to subscription_config_version: {}", e.getMessage());
            }
        }

        // 9. 检查并补充 user_subscription_period 表的 platform_code
        if (!isColumnExist("user_subscription_period", "platform_code")) {
            try {
                jdbcTemplate.execute("ALTER TABLE user_subscription_period ADD COLUMN platform_code VARCHAR(32) NOT NULL DEFAULT 'rocnovel'");
                log.info("Successfully added platform_code to user_subscription_period");
            } catch (Exception e) {
                log.warn("Failed to add platform_code to user_subscription_period: {}", e.getMessage());
            }
        }

        // 10. 历史数据平滑迁移与补齐：将历史遗留数据中 platform_code 为 NULL 或 空 或 'ALL' 的历史记录统一补齐更新为 'rocnovel'
        try {
            jdbcTemplate.execute("UPDATE IGNORE user_landing_page SET platform_code = 'rocnovel' WHERE platform_code IS NULL OR platform_code = '' OR platform_code = 'ALL'");
            jdbcTemplate.execute("UPDATE IGNORE raw_order SET platform_code = 'rocnovel' WHERE platform_code IS NULL OR platform_code = '' OR platform_code = 'ALL'");
            jdbcTemplate.execute("UPDATE IGNORE ltv_launch_config SET platform_code = 'rocnovel' WHERE platform_code IS NULL OR platform_code = '' OR platform_code = 'ALL'");
            jdbcTemplate.execute("UPDATE IGNORE subscription_config_version SET platform_code = 'rocnovel' WHERE platform_code IS NULL OR platform_code = '' OR platform_code = 'ALL'");
            jdbcTemplate.execute("UPDATE IGNORE user_subscription_period SET platform_code = 'rocnovel' WHERE platform_code IS NULL OR platform_code = '' OR platform_code = 'ALL'");
            // 清理此前因落地页/订单未关联而产生的 rocnovel 全零错误缓存行，以便重新聚合计算
            jdbcTemplate.execute("DELETE FROM ltv_daily_stat WHERE platform_code = 'rocnovel' AND total_recharge = 0 AND spend = 0");
            jdbcTemplate.execute("DELETE FROM daily_recharge_distribution WHERE platform_code = 'rocnovel' AND total_recharge = 0");
            log.info("Successfully backfilled historical platform_code = 'rocnovel' and cleaned empty cache rows");
        } catch (Exception e) {
            log.warn("Failed to backfill historical platform_code: {}", e.getMessage());
        }

        // 11. 检查并补充 raw_order 表的 UTC 时间字段及索引
        if (!isColumnExist("raw_order", "register_time_utc")) {
            try {
                jdbcTemplate.execute("ALTER TABLE raw_order ADD COLUMN register_time_utc DATETIME DEFAULT NULL AFTER register_date_et");
                jdbcTemplate.execute("ALTER TABLE raw_order ADD COLUMN register_date_utc DATE DEFAULT NULL AFTER register_time_utc");
                jdbcTemplate.execute("ALTER TABLE raw_order ADD COLUMN pay_time_utc DATETIME DEFAULT NULL AFTER pay_date_et");
                jdbcTemplate.execute("ALTER TABLE raw_order ADD COLUMN pay_date_utc DATE DEFAULT NULL AFTER pay_time_utc");
                log.info("Successfully added UTC datetime and date columns to raw_order");
            } catch (Exception e) {
                log.warn("Failed to add UTC columns to raw_order: {}", e.getMessage());
            }
        }
        try {
            if (!isIndexExist("raw_order", "idx_reg_date_utc")) {
                jdbcTemplate.execute("ALTER TABLE raw_order ADD INDEX idx_reg_date_utc (register_date_utc)");
                log.info("Successfully added index idx_reg_date_utc to raw_order");
            }
        } catch (Exception e) {
            log.info("raw_order idx_reg_date_utc index info: {}", e.getMessage());
        }

        // 12. 历史数据平滑回填与时区升级 (BJ -> CST / flicknovel -> UTC)
        try {
            jdbcTemplate.execute("UPDATE raw_order " +
                    "SET register_time_utc = CONVERT_TZ(register_time_bj, '+08:00', '+00:00'), " +
                    "    register_date_utc = DATE(CONVERT_TZ(register_time_bj, '+08:00', '+00:00')), " +
                    "    pay_time_utc = CONVERT_TZ(pay_time_bj, '+08:00', '+00:00'), " +
                    "    pay_date_utc = DATE(CONVERT_TZ(pay_time_bj, '+08:00', '+00:00')) " +
                    "WHERE pay_time_utc IS NULL AND pay_time_bj IS NOT NULL");

            jdbcTemplate.execute("UPDATE user_landing_page SET timezone = 'CST' WHERE timezone = 'BJ' AND platform_code != 'flicknovel'");
            jdbcTemplate.execute("UPDATE user_landing_page SET timezone = 'UTC' WHERE platform_code = 'flicknovel' AND (timezone = 'BJ' OR timezone IS NULL)");
            log.info("Successfully backfilled raw_order UTC timestamps and upgraded user_landing_page timezones");
        } catch (Exception e) {
            log.warn("Failed to backfill UTC timestamps or upgrade user_landing_page timezones: {}", e.getMessage());
        }

        // 13. 检查并创建 flicknovel_relation 表
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS flicknovel_relation (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "relation_id VARCHAR(64) NOT NULL, " +
                    "device_id VARCHAR(128) NOT NULL, " +
                    "promotion_id VARCHAR(64), " +
                    "promotion_code VARCHAR(64), " +
                    "ad_id VARCHAR(64), " +
                    "adset_id VARCHAR(64), " +
                    "campaign_id VARCHAR(64), " +
                    "ad_account_id VARCHAR(64), " +
                    "relation_begin_time_bj DATETIME, " +
                    "relation_begin_time_et DATETIME, " +
                    "relation_begin_date_et DATE, " +
                    "relation_begin_timestamp BIGINT, " +
                    "media_channel VARCHAR(64), " +
                    "platform VARCHAR(32), " +
                    "app_id VARCHAR(32), " +
                    "raw_payload TEXT, " +
                    "created_at DATETIME, " +
                    "updated_at DATETIME, " +
                    "UNIQUE KEY uk_fn_relation_id (relation_id), " +
                    "KEY idx_fn_device_id (device_id), " +
                    "KEY idx_fn_promotion_id (promotion_id), " +
                    "KEY idx_fn_begin_time_bj (relation_begin_time_bj), " +
                    "KEY idx_fn_begin_date_et (relation_begin_date_et), " +
                    "KEY idx_fn_media_channel (media_channel)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            log.info("Successfully checked/created flicknovel_relation table");
        } catch (Exception e) {
            log.warn("Failed to check/create flicknovel_relation table: {}", e.getMessage());
        }
    }

    private boolean isIndexExist(String tableName, String indexName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.statistics " +
                         "WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName, indexName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
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
