package com.webank.wecube.platform.core.boot;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDatabaseInitializer implements DatabaseInitializer {
    public static String[] JDBC_METADATA_TABLE_TYPES = { "TABLE" };
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected String strategy;
    protected DataSource dataSource;

    protected Connection connection;
    protected String dbSchema;
    
    protected List<String> existsTableNames = new ArrayList<String>();

    public AbstractDatabaseInitializer(String strategy, DataSource dataSource) {
        super();
        this.strategy = strategy == null ? STRATEGY_UPDATE : strategy.trim().toLowerCase();
        this.dataSource = dataSource;

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
            this.dbSchema = tryCalculateDbSchema();
            validateDbSchema();
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

    protected void doInitialize() throws SQLException {

        DbSchemaLockPropertyInfo lockInfo = tryAquireDbInitLock();
        if (lockInfo == null) {
            log.info("Did not aquired database initialization lock.");
            return;
        }
        
        getTableNamesPresent();

        if (STRATEGY_DROP_CREATE.equals(strategy)) {
            executeDbDropTables();
        }

        executeDbCreateTables();

        tryReleaseDbInitLock(lockInfo);
    }

    protected void executeDbCreateTables() {

    }

    protected void executeDbDropTables() {
        return;
    }

    protected void validateStrategy() {
        if (STRATEGY_NONE.equals(strategy) || STRATEGY_UPDATE.equals(strategy)
                || STRATEGY_DROP_CREATE.equals(strategy)) {
            return;
        }

        throw new ApplicationInitializeException("Unsupported strategy:" + strategy);
    }

    protected void validateDbSchema() {
        if (StringUtils.isBlank(dbSchema)) {
            throw new ApplicationInitializeException("Unkown database schema");
        }
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

    protected int dbInsertData(String sql, Object[] params) throws SQLException {
        return executeUpdate(sql, params);
    }

    protected int dbInsertData(String sql) throws SQLException {
        return executeUpdate(sql);
    }

    protected int executeUpdate(String sql) throws SQLException {
        Statement jdbcStatement = null;
        try {
            jdbcStatement = connection.createStatement();
            return jdbcStatement.executeUpdate(sql);
        } finally {
            closeSilently(jdbcStatement);
        }
    }

    protected int executeUpdate(String sql, Object[] params) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
            }

            return statement.executeUpdate();
        } finally {
            closeSilently(statement);
        }
    }

    protected void dbCreateTable(String createStatement) throws SQLException {
        Statement jdbcStatement = null;
        try {
            jdbcStatement = connection.createStatement();
            jdbcStatement.execute(createStatement);
        } finally {
            closeSilently(jdbcStatement);
        }
    }

    protected int dbUpdateTable(String sql) throws SQLException {
        return executeUpdate(sql);
    }

    protected int dbUpdateTable(String sql, Object[] params) throws SQLException {
        return executeUpdate(sql, params);
    }

    protected <T> List<T> queryList(String sql, Object[] params, LocalResultSetHandler<T> handler) throws SQLException {
        List<T> results = new ArrayList<T>();
        PreparedStatement queryStatement = null;
        ResultSet resultSet = null;
        try {
            queryStatement = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    queryStatement.setObject(i + 1, params[i]);
                }
            }
            resultSet = queryStatement.executeQuery();
            while (resultSet.next()) {
                T result = handler.handle(resultSet);
                results.add(result);
            }
        } finally {
            closeSilently(resultSet);
            closeSilently(queryStatement);
        }
        return results;
    }

    protected abstract String tryCalculateDbSchema();

    protected abstract DbSchemaLockPropertyInfo tryAquireDbInitLock();

    protected abstract void tryReleaseDbInitLock(DbSchemaLockPropertyInfo lockInfo);

    protected abstract boolean isTablePresent(String tableName);
    
    protected abstract void getTableNamesPresent() throws SQLException;

}
