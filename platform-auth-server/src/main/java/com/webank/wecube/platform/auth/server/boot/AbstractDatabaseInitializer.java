package com.webank.wecube.platform.auth.server.boot;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDatabaseInitializer implements DatabaseInitializer {
    public static final String[] JDBC_METADATA_TABLE_TYPES = { "TABLE" };
    public static final String PROPERTY_NAME_DB_VERSION = "db.schema.version";
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected String strategy;
    protected DataSource dataSource;

    protected Connection connection;
    protected String dbSchema;

    protected List<String> existsTableNames = new ArrayList<String>();
    
    protected ApplicationVersionInfo applicationVersionInfo;
    protected FileSystem fileSystem;

    public AbstractDatabaseInitializer(String strategy, DataSource dataSource,  ApplicationVersionInfo applicationVersionInfo) {
        super();
        this.strategy = strategy == null ? STRATEGY_UPDATE : strategy.trim().toLowerCase();
        this.dataSource = dataSource;
        this.applicationVersionInfo = applicationVersionInfo;

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
            closeSilently(fileSystem);
        }

    }

    @Override
    public String getInitializeStrategy() {
        return this.strategy;
    }
    
    protected List<Path> listUpgradeFiles() {

        try {
            return doListUpgradeFiles();
        } catch (IOException | URISyntaxException e) {
            log.error("Failed to list upgrade files", e);
            return Collections.emptyList();
        }
    }

    protected List<Path> doListUpgradeFiles() throws IOException, URISyntaxException {
        List<Path> upgradeSqlPaths = new ArrayList<Path>();
        CodeSource src = AbstractDatabaseInitializer.class.getProtectionDomain().getCodeSource();

        if (src == null) {
            log.error("Failed to get code source.");
            return upgradeSqlPaths;
        }

        URI uri = src.getLocation().toURI();
        Path myPath = null;
        if (uri.getScheme().equals("jar")) {
            if (fileSystem == null) {
                fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            }
            myPath = fileSystem.getPath("/db/upgrade");

            if (!Files.exists(myPath)) {
                myPath = fileSystem.getPath("/BOOT-INF/classes/db/upgrade");
            }

        } else {
            URL dirUrl = AbstractDatabaseInitializer.class.getClassLoader().getResource("db/upgrade");
            if (dirUrl != null) {
                URI dirUri = dirUrl.toURI();
                myPath = Paths.get(dirUri);
            }
        }

        if (myPath == null) {
            log.error("failed to find out path.");
            return upgradeSqlPaths;
        }

        if (!Files.exists(myPath)) {
            return upgradeSqlPaths;
        }

        Files.walk(myPath, 1).forEach(p -> {
            if (!Files.isDirectory(p) && p.getFileName().toString().endsWith(".sql")) {
                upgradeSqlPaths.add(p);
            }
        });

        return upgradeSqlPaths;
    }

    protected void doInitialize() throws SQLException {

        DbSchemaLockPropertyInfo lockInfo = tryAquireDbInitLock();
        if (lockInfo == null) {
            log.info("Did not aquired database initialization lock.");
            return;
        }

        log.info("try to initialize database:{}", dbSchema);
        try {
            AppPropertyInfo dbVersion = getDbVersion();
            log.info("db version:{}", dbVersion);
            if (dbVersion != null) {
                tryExecuteDbUpgrade(dbVersion);
                return;
            }
            

            if (STRATEGY_DROP_CREATE.equals(strategy)) {
                executeDbDropTables();
            }

            getTableNamesPresent();

            executeDbCreateTables();
            tryExecuteDbUpgrade();
            
            logDbVersion(dbVersion);
        } finally {
            tryReleaseDbInitLock(lockInfo);
        }
    }
    
    protected void tryExecuteDbUpgrade() {
        //
    }

    protected void tryExecuteDbUpgrade(AppPropertyInfo dbVersion) {
        //
    }

    protected boolean verifyVersionFormat(String versionInFile) {
        //
        return true;
    }

    protected void executeDbCreateTables() {
        executeDbCreateTableSchemas();
        executeDbCreateTableData();

    }

    protected void executeDbDropTables() {
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
        executeStatement(createStatement);
    }

    protected void executeStatement(String statement) throws SQLException {
        Statement jdbcStatement = null;
        try {
            jdbcStatement = connection.createStatement();
            jdbcStatement.execute(statement);
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

    protected String readNextTrimmedLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line != null) {
            line = line.trim();
        }
        return line;
    }

    protected String addSqlStatementPiece(String sqlStatement, String line) {
        if (sqlStatement == null) {
            return line;
        }
        return sqlStatement + " \n" + line;
    }

    protected byte[] readInputStream(InputStream inputStream, String inputStreamName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[16 * 1024];
        try {
            int bytesRead = inputStream.read(buffer);
            while (bytesRead != -1) {
                outputStream.write(buffer, 0, bytesRead);
                bytesRead = inputStream.read(buffer);
            }
        } catch (Exception e) {
            log.error("", e);
            throw new ApplicationInitializeException(e);
        }
        return outputStream.toByteArray();
    }

    protected abstract String tryCalculateDbSchema();

    protected abstract DbSchemaLockPropertyInfo tryAquireDbInitLock();

    protected abstract void tryReleaseDbInitLock(DbSchemaLockPropertyInfo lockInfo);

    protected abstract boolean isTablePresent(String tableName);

    protected abstract void getTableNamesPresent() throws SQLException;

    protected abstract void executeDbCreateTableSchemas();

    protected abstract void executeDbCreateTableData();

    protected abstract AppPropertyInfo getDbVersion();

    protected abstract void logDbVersion(AppPropertyInfo dbVersion);

}
