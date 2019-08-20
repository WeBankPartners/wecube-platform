package com.webank.wecube.core.service.resource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Service
@Slf4j
public class MysqlManagementService {

    private Connection connection;
    private DriverManagerDataSource dataSource;

    public MysqlManagementService createMysqlClient(String host, String port, String username, String password) {
        this.dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://" + host + ":" + port);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return this;
    }

    public void connect() throws SQLException {
        if (dataSource != null) {
            connection = dataSource.getConnection();
        }
    }

    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public void createSchema(String dbName) throws SQLException {
        try (Statement statement = connection.createStatement();) {
            statement.executeUpdate(String.format("CREATE SCHEMA %s", dbName));
        } catch (SQLException e) {
            log.error(String.format("Failed to delete schema [%s], meet error [%s].", dbName, e.getMessage()));
            throw e;
        }
    }

    public void deleteSchema(String dbName) throws SQLException {
        try (Statement statement = connection.createStatement();) {
            statement.executeUpdate(String.format("DROP SCHEMA %s", dbName));
        } catch (SQLException e) {
            log.error(String.format("Failed to delete schema [%s], meet error [%s].", dbName, e.getMessage()));
            throw e;
        }
    }

    public boolean hasTables(String dbName) throws SQLException {
        boolean hasTable = false;
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(String.format("SHOW TABLES FROM %s", dbName));) {
            resultSet.last();
            hasTable = resultSet.getRow() > 0;
        } catch (SQLException e) {
            log.error(String.format("Failed to query tables, meet error [%s].", e.getMessage()));
            throw e;
        }
        return hasTable;
    }
}
