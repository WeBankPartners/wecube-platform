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

    public MysqlManagementService newMysqlClient(String host, String port, String username, String password) {
        this.dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "?characterEncoding=utf8&serverTimezone=UTC");
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return this;
    }

    public MysqlManagementService connect() throws SQLException {
        if (dataSource != null) {
            connection = dataSource.getConnection();
        }
        return this;
    }

    public MysqlManagementService disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
        }
        return this;
    }

    public MysqlManagementService createSchema(String dbName) throws SQLException {
        try (Statement statement = connection.createStatement();) {
            statement.executeUpdate(String.format("CREATE SCHEMA %s", dbName));
        } catch (SQLException e) {
            log.error(String.format("Failed to delete schema [%s], meet error [%s].", dbName, e.getMessage()));
            throw e;
        }
        return this;
    }

    public MysqlManagementService createAccount(String username, String password, String dbName) throws SQLException {
        try (Statement statement = connection.createStatement();) {
            statement.executeUpdate(String.format("CREATE USER `%s` IDENTIFIED BY '%s'", username, password));
            statement.executeUpdate(String.format("GRANT ALL ON %s.* TO %s", dbName, username));
        } catch (SQLException e) {
            log.error(String.format("Failed to create account [username = %s] of database [%s].", username, dbName));
            throw e;
        }
        return this;
    }

    public MysqlManagementService deleteSchema(String dbName) throws SQLException {
        try (Statement statement = connection.createStatement();) {
            statement.executeUpdate(String.format("DROP SCHEMA %s", dbName));
        } catch (SQLException e) {
            log.error(String.format("Failed to delete schema [%s], meet error [%s].", dbName, e.getMessage()));
            throw e;
        }
        return this;
    }

    public MysqlManagementService deleteAccount(String username) throws SQLException {
        try (Statement statement = connection.createStatement();) {
            statement.executeUpdate(String.format("DROP USER %s", username));
        } catch (SQLException e) {
            log.error(String.format("Failed to drop account [username = %s]", username));
            throw e;
        }
        return this;
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
