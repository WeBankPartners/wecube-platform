package com.webank.wecube.platform.auth.server.authentication;

import org.apache.commons.lang3.StringUtils;
import org.forgerock.opendj.ldap.Connection;
import org.forgerock.opendj.ldap.LDAPConnectionFactory;
import org.forgerock.opendj.ldap.requests.BindRequest;
import org.forgerock.opendj.ldap.requests.Requests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.webank.wecube.platform.auth.server.model.LdapUmAuthContext;

public class LdapUmAuthenticationChecker {
    private static final Logger log = LoggerFactory.getLogger(LdapUmAuthenticationChecker.class);

    public void checkAuthentication(LdapUmAuthContext ctx, UsernamePasswordAuthenticationToken authToken) {

        verifyAuthToken(authToken);

        LDAPConnectionFactory connFactory = null;
        Connection conn = null;

        try {
            connFactory = new LDAPConnectionFactory(ctx.getHost(), ctx.getPort());
            try {
                conn = connFactory.getConnection();
            } catch (Exception e) {
                log.warn("failed to create connection for {} {}", ctx.getHost(), ctx.getPort());
                throw new AuthenticationServiceException("Cannot connect to LDAP server.");
            }

            String username = authToken.getName();
            String password = (String) authToken.getCredentials();

            BindRequest request = Requests.newSimpleBindRequest(username, password.getBytes());

            try {
                conn.bind(request);
            } catch (Exception e) {
                log.debug("authentication failed {} ", e.getMessage());
                throw new BadCredentialsException("Bad credential:" + e.getMessage());
            }
        } finally {
            if (conn != null) {
                conn.close();
            }

            if (connFactory != null) {
                connFactory.close();
            }
        }
    }

    private void verifyAuthToken(UsernamePasswordAuthenticationToken authToken) {
        String username = authToken.getName();
        String password = (String) authToken.getCredentials();

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BadCredentialsException("Bad credential:blank username or password.");
        }
    }
}
