package com.webank.wecube.core.service.resource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.ResourceItem;
import com.webank.wecube.core.service.CmdbResourceService;
import com.webank.wecube.core.utils.EncryptionUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MysqlAccountManagementService implements ResourceItemService {

    @Autowired
    private CmdbResourceService cmdbResourceService;

    public DriverManagerDataSource newMysqlDatasource(String host, String port, String username, String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "?characterEncoding=utf8&serverTimezone=UTC");
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Override
    public ResourceItem createItem(ResourceItem item) {
        Map<String, String> additionalProperties = item.getAdditionalPropertiesMap();
        String username = additionalProperties.get("username");
        String password = additionalProperties.get("password");
        if (username == null || password == null) {
            throw new WecubeCoreException("Username or password is missing");
        }

        DriverManagerDataSource dataSource = newDatasource(item);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();) {
            String rawPassword = EncryptionUtils.decryptWithAes(password, cmdbResourceService.getSeedFromSystemEnum(), item.getName());
            statement.executeUpdate(String.format("CREATE USER `%s` IDENTIFIED BY '%s'", item.getName(), rawPassword));
            statement.executeUpdate(String.format("GRANT ALL ON %s.* TO %s", item.getName(), username));
        } catch (Exception e) {
            String errorMessage = String.format("Failed to create account [username = %s]", item.getName());
            log.error(errorMessage);
            throw new WecubeCoreException(errorMessage, e);
        }
        return item;
    }

    @Override
    public void deleteItem(ResourceItem item) {
        DriverManagerDataSource dataSource = newDatasource(item);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();) {
            statement.executeUpdate(String.format("DROP USER %s", item.getName()));
        } catch (SQLException e) {
            String errorMessage = String.format("Failed to drop account [username = %s]", item.getName());
            log.error(errorMessage);
            throw new WecubeCoreException(errorMessage, e);
        }
    }

    private DriverManagerDataSource newDatasource(ResourceItem item) {
        String password;
        try {
            password = EncryptionUtils.decryptWithAes(item.getResourceServer().getLoginPassword(), cmdbResourceService.getSeedFromSystemEnum(), item.getResourceServer().getName());
        } catch (Exception e) {
            throw new WecubeCoreException(String.format("Failed to decrypt the login password of server [%s].", item.getResourceServer()), e);
        }

        DriverManagerDataSource dataSource = newMysqlDatasource(
                item.getResourceServer().getHost(),
                item.getResourceServer().getPort(),
                item.getResourceServer().getLoginUsername(),
                password);
        return dataSource;
    }

    @Override
    public ResourceItem retrieveItem(ResourceItem item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceItem updateItem(ResourceItem item) {
        throw new UnsupportedOperationException();
    }
}
