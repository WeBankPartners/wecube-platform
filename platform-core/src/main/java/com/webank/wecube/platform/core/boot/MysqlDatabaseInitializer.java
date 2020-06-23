package com.webank.wecube.platform.core.boot;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * NOT thread-safe
 * 
 * @author gavin
 *
 */
final class MysqlDatabaseInitializer extends AbstractDatabaseInitializer {

    public static final String CREATE_SQL_NAME = "1.wecube.mysql.create.sql";
    public static final String DROP_SQL_NAME = "1.wecube.mysql.drop.sql";

    private String createAppPropertyInfoSql;
    private String insertAppPropertyInfoSql;
    private String queryAppPropertyInfoSql;
    private String updateAppPropertyInfoSql;

    MysqlDatabaseInitializer(String strategy, DataSource dataSource) {
        super(strategy, dataSource);
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
        try {
            int ret = dbUpdateTable(updateAppPropertyInfoSql, new Object[] { DbSchemaLockPropertyInfo.VAL_UNLOCK,
                    lockInfo.getNextRev(), lockInfo.getName(), lockInfo.getRev() });
            if (ret <= 0) {
                throw new ApplicationInitializeException("Update table failed");
            }
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
    }

}
