package com.webank.wecube.platform.core.boot;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDatabaseInitializer implements DatabaseInitializer {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected String strategy;
    protected DataSource dataSource;

    protected Connection connection;
    protected String dbSchema;

    public AbstractDatabaseInitializer(String strategy, DataSource dataSource) {
        super();
        this.strategy = strategy == null ? STRATEGY_UPDATE : strategy.trim().toLowerCase();
        this.dataSource = dataSource;

        this.dbSchema = tryCalculateDbSchema();
        validateStrategy();
    }

    @Override
    public void initialize() {
        log.info("Database initialize strategy:{}", strategy);
        if (STRATEGY_NONE.equals(strategy)) {
            return;
        }

        try {
            this.connection = dataSource.getConnection();
            doInitialize();
        } catch (Exception e) {
            log.error("", e);
            throw new ApplicationInitializeException(e);
        } finally {
            closeSilently(this.connection);
        }

    }

    @Override
    public String getInitializeStrategy() {
        return this.strategy;
    }

    protected void doInitialize() {

    }

    protected void validateStrategy() {
        if (STRATEGY_NONE.equals(strategy) || STRATEGY_UPDATE.equals(strategy)
                || STRATEGY_DROP_CREATE.equals(strategy)) {
            return;
        }

        throw new ApplicationInitializeException("Unsupported strategy:" + strategy);
    }

    protected void closeSilently(AutoCloseable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    protected void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                log.error("", e);
            }
        }
    }

    protected abstract String tryCalculateDbSchema();

}
