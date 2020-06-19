package com.webank.wecube.platform.core.commons;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author gavin
 *
 */
public final class AuthenticationContextHolder {

    private static final ThreadLocal<AuthenticatedUser> currentUser = new InheritableThreadLocal<>();

    public static boolean setAuthenticatedUser(AuthenticatedUser u) {
        if (u == null) {
            return false;
        }

        if (currentUser.get() != null) {
            return false;
        }

        currentUser.set(u);
        return true;
    }

    public static AuthenticatedUser getCurrentUser() {
        return currentUser.get();
    }

    public static void clearCurrentUser() {
        currentUser.remove();
    }

    public static String getCurrentUsername() {
        AuthenticatedUser u = currentUser.get();
        if (u != null) {
            return u.getUsername();
        }

        return null;
    }

    public static Set<String> getCurrentUserRoles() {
        AuthenticatedUser u = currentUser.get();
        if (u != null) {
            return u.getAuthorities();
        }

        return null;
    }

    public static class AuthenticatedUser {
        private final String username;
        private final String token;
        private final Set<String> grantedAuthorities = new HashSet<String>();

        public AuthenticatedUser(String username) {
            this(username, null);
        }

        public AuthenticatedUser(String username, String token) {
            this(username, token, null);
        }

        public AuthenticatedUser(String username, String token, Collection<String> authorities) {
            super();
            this.username = username;
            this.token = token;
            if (authorities != null) {
                for (String a : authorities) {
                    if (a != null) {
                        this.grantedAuthorities.add(a);
                    }
                }
            }
        }

        public String getUsername() {
            return username;
        }

        public Set<String> getAuthorities() {
            return Collections.unmodifiableSet(this.grantedAuthorities);
        }

        public String getToken() {
            return token;
        }

    }

}
