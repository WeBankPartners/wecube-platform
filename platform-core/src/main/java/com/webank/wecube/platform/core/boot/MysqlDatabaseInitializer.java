package com.webank.wecube.platform.core.boot;

import java.sql.Connection;
import java.sql.SQLException;

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

    MysqlDatabaseInitializer(String strategy, DataSource dataSource) {
        super(strategy, dataSource);

    }

    protected String tryCalculateDbSchema() {
        Connection conn = null;
        try {
            conn = this.dataSource.getConnection();
            String jdbcUrl = connection.getMetaData().getURL().toString();
            String strSchema = jdbcUrl.substring(0, jdbcUrl.indexOf("?"));
            strSchema = strSchema.substring(strSchema.lastIndexOf("/") + 1);
            return strSchema;
        } catch (SQLException e) {
            log.error("", e);
            throw new ApplicationInitializeException(e);
        } finally {
            closeSilently(conn);
        }

    }

}
