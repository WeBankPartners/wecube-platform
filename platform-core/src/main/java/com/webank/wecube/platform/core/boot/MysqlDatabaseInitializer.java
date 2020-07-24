package com.webank.wecube.platform.core.boot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

/**
 * NOT thread-safe
 * 
 * @author gavin
 *
 */
final class MysqlDatabaseInitializer extends AbstractDatabaseInitializer {

    public static final String CREATE_SCHEMA_NAME = "01.wecube.mysql.create.schema.sql";
    public static final String DROP_SCHEMA_NAME = "01.wecube.mysql.drop.sql";
    public static final String CREATE_DATA_NAME = "02.wecube.mysql.create.data.sql";

    private String createAppPropertyInfoSql;
    private String insertAppPropertyInfoSql;
    private String queryAppPropertyInfoSql;
    private String updateAppPropertyInfoSql;

    private StatementInfoParser statementInfoParser = new MysqlStatementInfoParser();

    private VersionComparator versionComparator = new VersionComparator();

    MysqlDatabaseInitializer(String strategy, DataSource dataSource, ApplicationVersionInfo applicationVersionInfo) {
        super(strategy, dataSource, applicationVersionInfo);
        this.createAppPropertyInfoSql = createAppPropertyInfoSql();
        this.insertAppPropertyInfoSql = insertAppPropertyInfoSql();
        this.queryAppPropertyInfoSql = queryAppPropertyInfoSql();
        this.updateAppPropertyInfoSql = updateAppPropertyInfoSql();

    }

    private String updateAppPropertyInfoSql() {
        String sql = "UPDATE %s SET val=?,rev=? where name=? and rev=?";
        return String.format(sql, AppPropertyInfo.TABLE_NAME);
    }

    private String queryAppPropertyInfoSql() {
        String sql = "SELECT name,val,rev FROM %s WHERE name=?";
        return String.format(sql, AppPropertyInfo.TABLE_NAME);
    }

    private String insertAppPropertyInfoSql() {
        String sqlStatement = "insert into %s (name, val, rev) values (?,?,?)";
        return String.format(sqlStatement, AppPropertyInfo.TABLE_NAME);
    }

    private String createAppPropertyInfoSql() {
        String sqlStatement = "CREATE TABLE %s "
                + "(name varchar(100) NOT NULL, val varchar(300) DEFAULT NULL,rev int(11) DEFAULT NULL, PRIMARY KEY (name) ) "
                + "ENGINE=InnoDB DEFAULT CHARSET=utf8";
        return String.format(sqlStatement, AppPropertyInfo.TABLE_NAME);
    }

    protected String tryCalculateDbSchema() {
        try {
            String jdbcUrl = connection.getMetaData().getURL().toString();
            String strSchema = jdbcUrl.substring(0, jdbcUrl.indexOf("?"));
            strSchema = strSchema.substring(strSchema.lastIndexOf("/") + 1);
            return strSchema;
        } catch (SQLException e) {
            log.error("", e);
            throw new ApplicationInitializeException(e);
        }

    }

    @Override
    protected DbSchemaLockPropertyInfo tryAquireDbInitLock() {
        boolean existAppPropertyInfoTable = isTablePresent(AppPropertyInfo.TABLE_NAME);
        DbSchemaLockPropertyInfo lockInfo = null;
        if (!existAppPropertyInfoTable) {
            lockInfo = tryCreateAppPropertyTable();
        } else {
            lockInfo = tryLockDbInitLock();
        }
        return lockInfo;
    }

    private DbSchemaLockPropertyInfo tryLockDbInitLock() {
        LocalResultSetHandler<DbSchemaLockPropertyInfo> handler = (r) -> {
            DbSchemaLockPropertyInfo lockInfo = new DbSchemaLockPropertyInfo(r.getString("val"), r.getInt("rev"));
            return lockInfo;
        };
        try {
            List<DbSchemaLockPropertyInfo> locks = queryList(queryAppPropertyInfoSql,
                    new Object[] { DbSchemaLockPropertyInfo.PROPERTY_NAME }, handler);

            if (locks.isEmpty()) {
                return null;
            }

            DbSchemaLockPropertyInfo lock = locks.get(0);
            if (DbSchemaLockPropertyInfo.VAL_LOCK.equals(lock.getVal())) {
                return null;
            }

            int ret = dbUpdateTable(updateAppPropertyInfoSql, new Object[] { DbSchemaLockPropertyInfo.VAL_UNLOCK,
                    lock.getNextRev(), lock.getName(), lock.getRev() });
            if (ret <= 0) {
                return null;
            }

            lock.setRev(lock.getNextRev());
            return lock;
        } catch (SQLException e) {
            log.error("", e);
            return null;
        }
    }

    private DbSchemaLockPropertyInfo tryCreateAppPropertyTable() {
        try {
            dbCreateTable(this.createAppPropertyInfoSql);
            DbSchemaLockPropertyInfo lockInfo = new DbSchemaLockPropertyInfo(DbSchemaLockPropertyInfo.VAL_LOCK, 1);
            int ret = dbInsertData(insertAppPropertyInfoSql, lockInfo.unpack());
            if (ret <= 0) {
                return null;
            } else {
                return lockInfo;
            }

        } catch (SQLException e) {
            log.error("", e);
            return null;
        }
    }

    @Override
    protected void tryReleaseDbInitLock(DbSchemaLockPropertyInfo lockInfo) {
        log.info("try to release databse init lock:{}", lockInfo);
        if (lockInfo == null) {
            log.warn("Cannot get lock info");
            return;
        }
        try {
            int ret = dbUpdateTable(updateAppPropertyInfoSql, new Object[] { DbSchemaLockPropertyInfo.VAL_UNLOCK,
                    lockInfo.getNextRev(), lockInfo.getName(), lockInfo.getRev() });
            if (ret <= 0) {
                log.error("unlock failed,{}", lockInfo);
                throw new ApplicationInitializeException("Update table failed");
            }

            log.info("unlocked:{}", lockInfo);
        } catch (SQLException e) {
            throw new ApplicationInitializeException(e);
        }
    }

    @Override
    protected boolean isTablePresent(String tableName) {
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet tables = null;

            try {
                tables = databaseMetaData.getTables(null, this.dbSchema, tableName, JDBC_METADATA_TABLE_TYPES);
                return tables.next();
            } finally {
                if (tables != null) {
                    tables.close();
                }
            }

        } catch (Exception e) {
            throw new ApplicationInitializeException(e);
        }
    }

    @Override
    protected void getTableNamesPresent() throws SQLException {
        List<String> tableNames = new ArrayList<>();
        ResultSet tablesRs = null;

        try {

            DatabaseMetaData databaseMetaData = connection.getMetaData();
            tablesRs = databaseMetaData.getTables(null, dbSchema, null, JDBC_METADATA_TABLE_TYPES);
            while (tablesRs.next()) {
                String tableName = tablesRs.getString("TABLE_NAME");
                tableName = tableName.toUpperCase();
                tableNames.add(tableName);
            }
        } finally {
            if (tablesRs != null) {
                tablesRs.close();
            }
        }

        this.existsTableNames.addAll(tableNames);

        StringBuilder sb = new StringBuilder();
        sb.append("EXISTS TABLES:\n");
        for (String tableName : this.existsTableNames) {
            sb.append(tableName).append("\n");
        }

        log.info(sb.toString());
    }

    protected void executeDbDropTables() {
        log.info("start to execute drop tables");
        InputStream inputStream = null;
        String fileName = String.format("db/drop/%s", DROP_SCHEMA_NAME);
        try {
            inputStream = DatabaseInitializer.class.getClassLoader().getResourceAsStream(fileName);
            if (inputStream == null) {
                throw new ApplicationInitializeException("Can not read create table file:" + fileName);
            }
            executeSchemaResource("Drop", "core", "core", inputStream);
        } finally {
            closeSilently(inputStream);
        }

    }

    protected void executeDbCreateTableSchemas() {
        log.info("start to execute creating table schemas");
        InputStream inputStream = null;
        String fileName = String.format("db/create/%s", CREATE_SCHEMA_NAME);
        try {
            inputStream = DatabaseInitializer.class.getClassLoader().getResourceAsStream(fileName);
            if (inputStream != null) {
                executeSchemaResource("Create", "core", "schema", inputStream);
            } else {
                log.warn("Can not read create table schema file:{}", fileName);
            }
        } finally {
            closeSilently(inputStream);
        }
    }

    protected void executeDbCreateTableData() {
        log.info("start to execute creating table data");
        InputStream inputStream = null;
        String fileName = String.format("db/create/%s", CREATE_DATA_NAME);
        try {
            inputStream = DatabaseInitializer.class.getClassLoader().getResourceAsStream(fileName);
            if (inputStream != null) {
                executeSchemaResource("Create", "core", "data", inputStream);
            } else {
                log.warn("Can not read create table data file:{}", fileName);
            }
        } finally {
            closeSilently(inputStream);
        }
    }

    private void processStatementInfo(StatementInfo info) throws SQLException {
        if (DbOperationType.Create == info.getOperType()
                && this.existsTableNames.contains(info.getTableName().toUpperCase())) {
            log.info("table {} already exists", info.getTableName());
            return;
        }

        log.info("process {}", info);
        executeStatement(info.getStatement());
    }

    private void executeSchemaResource(String operation, String component, String resourceName,
            InputStream inputStream) {
        String sqlStatement = null;
        try {
            Exception exception = null;
            byte[] bytes = readInputStream(inputStream, resourceName);
            String ddlStatements = new String(bytes, Charset.forName("utf-8"));
            BufferedReader reader = new BufferedReader(new StringReader(ddlStatements));
            String line = readNextTrimmedLine(reader);

            List<String> logLines = new ArrayList<>();
            long lineNum = 0L;
            while (line != null) {
                lineNum++;
                if (line.startsWith("#")) {
                    logLines.add(line.substring(2));
                } else if (line.startsWith("--")) {
                    logLines.add(line.substring(3));
                } else if (line.length() > 0) {

                    if (line.endsWith(";")) {
                        sqlStatement = addSqlStatementPiece(sqlStatement, line.substring(0, line.length() - 1));
                        StatementInfo statementInfo = statementInfoParser.parseStatement(sqlStatement, lineNum);
                        try {
                            // no logging needed as the connection will log it
                            logLines.add(sqlStatement);
                            processStatementInfo(statementInfo);
                        } catch (Exception e) {
                            if (exception == null) {
                                exception = e;
                            }
                        } finally {
                            sqlStatement = null;
                        }
                    } else {
                        sqlStatement = addSqlStatementPiece(sqlStatement, line);
                    }
                }

                line = readNextTrimmedLine(reader);
            }

            if (exception != null) {
                throw exception;
            }

        } catch (Exception e) {
            throw new ApplicationInitializeException("execute sql errors", e);
        }
    }

    @Override
    protected AppPropertyInfo getDbVersion() {
        LocalResultSetHandler<AppPropertyInfo> handler = (r) -> {
            AppPropertyInfo obj = new AppPropertyInfo(r.getString("name"), r.getString("val"), r.getInt("rev"));
            return obj;
        };
        try {
            List<AppPropertyInfo> dbVersions = queryList(queryAppPropertyInfoSql,
                    new Object[] { PROPERTY_NAME_DB_VERSION }, handler);

            if (dbVersions.isEmpty()) {
                return null;
            }

            AppPropertyInfo dbVersion = dbVersions.get(0);

            return dbVersion;
        } catch (SQLException e) {
            log.error("", e);
            return null;
        }
    }

    @Override
    protected void logDbVersion(AppPropertyInfo dbVersion) {
        try {
            if (dbVersion == null) {
                AppPropertyInfo newDbVersion = new AppPropertyInfo(PROPERTY_NAME_DB_VERSION,
                        applicationVersionInfo.getVersion(), AppPropertyInfo.REV_INIT);
                int ret = dbInsertData(insertAppPropertyInfoSql, newDbVersion.unpack());

                if (ret <= 0) {
                    throw new ApplicationInitializeException("Failed to log db version.");
                }
            } else {
                updateDbVersionToAppVersion(dbVersion);
            }
        } catch (SQLException e) {
            throw new ApplicationInitializeException(e);
        }

    }

    protected void tryExecuteDbUpgrade() {
        List<Path> upgradeFilePaths = listUpgradeFiles();
        if (upgradeFilePaths == null || upgradeFilePaths.isEmpty()) {
            log.info("There is no upgrade files.");
            return;
        }

        log.info("upgradeFilePaths size:{}, and upgrade files:", upgradeFilePaths.size());

        for (Path path : upgradeFilePaths) {
            log.info("upgrade file:{}, {}", path.getFileName(), path.toAbsolutePath());
        }

        for (Path path : upgradeFilePaths) {
            try {
                doExecuteDbUpgrade(path);
            } catch (IOException e) {
                log.error("failed to execute:" + path.getFileName(), e);
            }
        }
    }

    private void doExecuteDbUpgrade(Path path) throws IOException {
        log.info("start to execute DB upgrade for :{}", path.getFileName());
        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(path);
            if (inputStream != null) {
                executeSchemaResource("Upgrade", "core", "data", inputStream);
            } else {
                log.warn("Can not upgrade DB data file:{}", path.getFileName());
            }
        } finally {
            closeSilently(inputStream);
        }
    }

    protected void tryExecuteDbUpgrade(AppPropertyInfo dbVersion) {
        if (dbVersion == null) {
            return;
        }

        log.info("start to upgrade DB from : {} to : {}", dbVersion.getVal(), applicationVersionInfo.getVersion());

        if(StringUtils.isBlank(dbVersion.getVal())){
            dbVersion.setVal("0.0.0");
        }
        
        String appVersion = applicationVersionInfo.getVersion();
        String lastDbVersion = dbVersion.getVal();
        if(versionComparator.compare(appVersion, lastDbVersion) <= 0){
            log.info("no need to upgrade DB data.");
            return;
        }

        List<Path> rawUpgradeFilePaths = listUpgradeFiles();
        if (rawUpgradeFilePaths == null || rawUpgradeFilePaths.isEmpty()) {
            log.info("There is no upgrade files.");
            updateDbVersionToAppVersion(dbVersion);
            return;
        }

        List<PathAndVersion> upgradeFilePaths = determineUpgradeFilePaths(dbVersion, rawUpgradeFilePaths);

        if (upgradeFilePaths.isEmpty()) {
            log.info("There is no filtered upgrade files.");
            updateDbVersionToAppVersion(dbVersion);
            return;
        }

        Collections.sort(upgradeFilePaths, new Comparator<PathAndVersion>() {

            @Override
            public int compare(PathAndVersion p1, PathAndVersion p2) {
                return versionComparator.compare(p1.getVersion(), p2.getVersion());
            }

        });

        log.info("upgradeFilePaths size:{}, and upgrade files:", upgradeFilePaths.size());
        for (PathAndVersion pv : upgradeFilePaths) {
            log.info("upgrade file:{}, {}", pv.getPath().getFileName(), pv.getPath().toAbsolutePath());
        }

        for (PathAndVersion pv : upgradeFilePaths) {
            try {
                doExecuteDbUpgrade(pv.getPath());
            } catch (IOException e) {
                log.error("failed to execute:" + pv.getPath().getFileName(), e);
            }
        }

        updateDbVersionToAppVersion(dbVersion);
    }

    private void updateDbVersionToAppVersion(AppPropertyInfo dbVersion) {
        try {
            if (dbVersion != null) {
                int ret = dbUpdateTable(updateAppPropertyInfoSql, new Object[] { applicationVersionInfo.getVersion(),
                        dbVersion.getNextRev(), dbVersion.getName(), dbVersion.getRev() });
                if (ret <= 0) {
                    throw new ApplicationInitializeException("Update table failed");
                }
            }
        } catch (SQLException e) {
            throw new ApplicationInitializeException(e);
        }
    }

    private List<PathAndVersion> determineUpgradeFilePaths(AppPropertyInfo dbVersion, List<Path> upgradeFilePaths) {
        String lastDbVersion = dbVersion.getVal();
        if (StringUtils.isBlank(lastDbVersion)) {
            lastDbVersion = "0.0.0";
        }

        String currDbVersion = applicationVersionInfo.getVersion();
        List<PathAndVersion> filteredPaths = new ArrayList<PathAndVersion>();
        for (Path p : upgradeFilePaths) {
            String fileName = p.getFileName().toString();
            String versionInFile = fileName.substring("anyway_".length(), fileName.lastIndexOf(".sql"));
            log.info("versionInFile:{}", versionInFile);
            if (StringUtils.isBlank(versionInFile)) {
                continue;
            }

            if (!verifyVersionFormat(versionInFile)) {
                continue;
            }

            if (needUpgrade(versionInFile, lastDbVersion, currDbVersion)) {
                PathAndVersion pv = new PathAndVersion(p, versionInFile);
                filteredPaths.add(pv);
            } else {
                log.info("abandoned to upgrade:{}", p.getFileName());
            }
        }
        return filteredPaths;
    }

    private boolean needUpgrade(String versionInFile, String lastDbVersion, String currDbVersion) {
        if (lastDbVersion.equals(currDbVersion)) {
            return false;
        }

        int compareToLastDbVersion = versionComparator.compare(versionInFile, lastDbVersion);
        int compareToCurrDbVersion = versionComparator.compare(versionInFile, currDbVersion);
        if (compareToLastDbVersion > 0 && compareToCurrDbVersion <= 0) {
            return true;
        }

        return false;
    }

    public static class PathAndVersion {
        private Path path;
        private String version;

        public PathAndVersion(Path path, String version) {
            super();
            this.path = path;
            this.version = version;
        }

        public Path getPath() {
            return path;
        }

        public String getVersion() {
            return version;
        }

    }

    public static class VersionComparator implements Comparator<String> {
        private static final String VERSION_PREFIX = "v";

        /**
         * 
         */
        @Override
        public int compare(String v1, String v2) {

            int[] v1Nums = tidyVersion(v1);
            int[] v2Nums = tidyVersion(v2);

            int compare = 0;
            for (int i = 0; i < v1Nums.length; i++) {
                compare = (v1Nums[i] - v2Nums[i]);
                if (compare != 0) {
                    break;
                }
            }

            return compare;
        }

        private int[] tidyVersion(String versionStr) {
            if (isBlank(versionStr)) {
                throw new IllegalArgumentException();
            }

            if (VERSION_PREFIX.equalsIgnoreCase(versionStr.substring(0, 1))) {
                versionStr = versionStr.substring(1);
            }

            String[] vStrNums = versionStr.split("\\.");
            int[] vNums = new int[] { 0, 0, 0, 0 };
            for (int i = 0; i < vStrNums.length && i < vNums.length; i++) {
                vNums[i] = Integer.parseInt(vStrNums[i]);
            }

            return vNums;
        }

        private boolean isBlank(String s) {
            if ((s == null) || (s.trim().length() < 1)) {
                return true;
            }

            return false;
        }

    }

}
