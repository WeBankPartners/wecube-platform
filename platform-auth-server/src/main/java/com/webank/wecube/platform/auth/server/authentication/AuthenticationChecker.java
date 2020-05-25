package com.webank.wecube.platform.auth.server.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationChecker {
	void checkAuthentication(UserDetails user, Authentication token);
}
