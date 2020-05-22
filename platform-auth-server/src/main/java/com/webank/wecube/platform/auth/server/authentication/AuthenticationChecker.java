package com.webank.wecube.platform.auth.server.authentication;

import org.springframework.security.core.Authentication;

import com.webank.wecube.platform.auth.server.model.AuthContext;

public interface AuthenticationChecker {
	void checkAuthentication(AuthContext ctx, Authentication authToken);
}
