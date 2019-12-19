package com.webank.wecube.platform.core.service.cmder.ssh2;

public interface PoolableRemoteCommandExecutor extends RemoteCommandExecutor {
	void init(RemoteCommandExecutorConfig config) throws Exception;
	void destroy();
}
