package com.webank.wecube.platform.core.service.cmder.ssh2;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;

@Service
public class ScpService {
    private static final Logger log = LoggerFactory.getLogger(ScpService.class);

    public boolean isAuthedWithPassword(String ip, Integer port, String user, String password) {
        Connection connection = new Connection(ip, port);
        try {
            return connection.authenticateWithPassword(user, password);
        } catch (IOException e) {
            log.error("errors while ssh authentication.", e);
        }
        return false;
    }

    public boolean isAuthedWithPublicKey(String ip, Integer port, String user, File privateKey, String password) {
        Connection connection = new Connection(ip, port);
        try {
            return connection.authenticateWithPublicKey(user, privateKey, password);
        } catch (IOException e) {
            log.error("errors while ssh authentication.", e);
        }
        return false;
    }

    public boolean isAuth(String ip, Integer port, String user, String password, String privateKey,
            boolean usePassword) {
        if (usePassword) {
            return isAuthedWithPassword(ip, port, user, password);
        } else {
            return isAuthedWithPublicKey(ip, port, user, new File(privateKey), password);
        }
    }

    public void getFile(String ip, Integer port, String user, String password, String privateKey, boolean usePassword,
            String remoteFile, String path) {
        Connection connection = new Connection(ip, port);
        try {
            connection.connect();
            boolean isAuthed = isAuth(ip, port, user, password, privateKey, usePassword);
            if (isAuthed) {
                log.info("Authentication is successful!");
                SCPClient scpClient = connection.createSCPClient();
                scpClient.get(remoteFile, path);
            } else {
                log.info("Authentication failed!");
            }
        } catch (IOException e) {
            log.error("errors while getting file.", e);
        } finally {
            connection.close();
        }
    }

    public void putFile(String ip, Integer port, String user, String password, String privateKey, boolean usePassword,
            String localFile, String remoteTargetDirectory) {
        log.info("Start to connect {}:{}", ip, port);
        Connection connection = new Connection(ip, port);
        try {
            connection.connect();
            boolean isAuthed = isAuth(ip, port, user, password, privateKey, usePassword);
            if (isAuthed) {
                SCPClient scpClient = connection.createSCPClient();
                scpClient.put(localFile, remoteTargetDirectory);
            } else {
                log.info("Authentication failed!");
            }
        } catch (Exception ex) {
            log.error("errors while putting file.", ex);
        } finally {
            connection.close();
        }
    }

    public void put(String ip, Integer port, String user, String password, String localFile,
            String remoteTargetDirectory) {
        Connection conn = new Connection(ip, port);
        try {
            conn.connect();
            boolean isconn = conn.authenticateWithPassword(user, password);
            if (isconn) {
                log.info("Connection is OK");
                SCPClient scpClient = conn.createSCPClient();
                log.info("scp local file [{}] to remote target directory [{}]", localFile, remoteTargetDirectory);
                scpClient.put(localFile, remoteTargetDirectory, "7777");
            } else {
                log.info("User or password incorrect");
                throw new WecubeCoreException("3222", "User or password incorrect");
            }
        } catch (Exception e) {
            log.error("errors while putting file.", e);
            throw new WecubeCoreException("3223", String.format("Run 'scp' command meet error: %s", e.getMessage()));
        } finally {
            conn.close();
        }
    }
}
