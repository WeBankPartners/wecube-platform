package com.webank.wecube.core.service.cmder.ssh2.impl;

import com.webank.wecube.core.service.cmder.ssh2.RemoteCommand;
import com.webank.wecube.core.service.cmder.ssh2.RemoteCommandExecutorConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class PooledRemoteCommandExecutorTest {

    @Test
    public void testExecuteRemoteCommand() throws Exception {
        RemoteCommandExecutorConfig config = new RemoteCommandExecutorConfig();
        config.setRemoteHost("10.255.18.23");
        config.setUser("root");
        config.setPsword("Ab888888");
        config.setPort(60000);

        String strCmd = "uname -a";
        RemoteCommand cmd0 = new SimpleRemoteCommand(strCmd);
        PooledRemoteCommandExecutor executor = new PooledRemoteCommandExecutor();
        executor.init(config);

        String result = executor.execute(cmd0);

        executor.destroy();

        System.out.println("\n===========================\n");
        System.out.println("result:" + result);
        System.out.println("\n===========================\n");
    }

    @Test
    public void testExecuteRemoteCommandAsynchronously() {
        //fail("Not yet implemented");
    }

}
