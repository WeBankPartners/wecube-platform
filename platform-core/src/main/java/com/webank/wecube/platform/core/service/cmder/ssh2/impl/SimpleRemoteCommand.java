package com.webank.wecube.platform.core.service.cmder.ssh2.impl;

import com.webank.wecube.platform.core.service.cmder.ssh2.RemoteCommand;

public class SimpleRemoteCommand implements RemoteCommand {
	private String simpleCommand;
	
	public SimpleRemoteCommand(String simpleCommand) {
		super();
		this.simpleCommand = simpleCommand;
	}

	@Override
	public String getCommand() {
		return this.simpleCommand;
	}

}
