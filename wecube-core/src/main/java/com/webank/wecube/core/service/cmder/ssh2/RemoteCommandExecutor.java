package com.webank.wecube.core.service.cmder.ssh2;

public interface RemoteCommandExecutor {
	String execute(RemoteCommand cmd);
	void execute(RemoteCommand cmd, boolean asynchorously);
}
