package com.webank.wecube.platform.core.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.service.cmder.ssh2.RemoteCommand;
import com.webank.wecube.platform.core.service.cmder.ssh2.RemoteCommandExecutorConfig;
import com.webank.wecube.platform.core.service.cmder.ssh2.impl.PooledRemoteCommandExecutor;
import com.webank.wecube.platform.core.service.cmder.ssh2.impl.SimpleRemoteCommand;

@Service
public class CommandService {
    private static final Logger log = LoggerFactory.getLogger(CommandService.class);

    public void runAtLocal(String command) throws Exception {
        String returnString = "";
        Process pro = null;
        Runtime runTime = Runtime.getRuntime();
        if (runTime == null) {
            throw new WecubeCoreException("3220","Create runtime false!");
        }
        try {
            pro = runTime.exec(command);
            BufferedReader input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            PrintWriter output = new PrintWriter(new OutputStreamWriter(pro.getOutputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                returnString = returnString + line + "\n";
            }
            input.close();
            output.close();
            pro.destroy();
        } catch (IOException e) {
            log.error("Execute '{}' command failed.{}", command, e);
        }
        log.info("Execute '{}' command successful", command);
    }

    public String runAtRemote(String host, String user, String password, Integer port, String command)
            throws Exception {
        RemoteCommandExecutorConfig config = new RemoteCommandExecutorConfig();
        config.setRemoteHost(host);
        config.setUser(user);
        config.setPsword(password);
        config.setPort(port);

        RemoteCommand cmd = new SimpleRemoteCommand(command);
        PooledRemoteCommandExecutor executor = new PooledRemoteCommandExecutor();
        executor.init(config);

        String result = executor.execute(cmd);

        executor.destroy();

        log.info("result is: " + result);
        return result;
    }

    public String runAtRemoteHasReturn(String host, String user, String password, Integer port, String command)
            throws Exception {
        RemoteCommandExecutorConfig config = new RemoteCommandExecutorConfig();
        config.setRemoteHost(host);
        config.setUser(user);
        config.setPsword(password);
        config.setPort(port);

        RemoteCommand cmd = new SimpleRemoteCommand(command);
        PooledRemoteCommandExecutor executor = new PooledRemoteCommandExecutor();
        executor.init(config);

        String result = executor.execute(cmd);

        executor.destroy();

        log.info("result is: " + result);
        if (result == "" || result.isEmpty()) {
            throw new WecubeCoreException("3221","return is empty, please check !");
        } else {
            return result;
        }
    }
}
