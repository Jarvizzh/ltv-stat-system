package com.ltv.stat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 数据库初始化组件
 * 历史动态 DDL 及数据清洗代码已清理，最新完整数据表结构请参阅 schema.sql
 */
@Component
public class DatabasePrimaryKeysInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabasePrimaryKeysInitializer.class);

    @PostConstruct
    public void init() {
        log.info("DatabasePrimaryKeysInitializer: Database schema is up to date.");
    }
}
