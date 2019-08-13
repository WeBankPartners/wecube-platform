package com.webank.wecube.core.service.cmder.ssh2;

public interface PoolableRemoteCommandExecutor extends RemoteCommandExecutor {
	void init(RemoteCommandExecutorConfig config) throws Exception;
	void destroy();
}
