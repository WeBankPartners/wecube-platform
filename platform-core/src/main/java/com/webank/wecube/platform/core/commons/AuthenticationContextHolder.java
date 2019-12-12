package com.webank.wecube.platform.core.commons;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    public static class AuthenticatedUser {
        private final String username;
        private Set<String> grantedAuthorities = new HashSet<String>();

        public AuthenticatedUser(String username) {
            super();
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public Set<String> getAuthorities() {
            return Collections.unmodifiableSet(this.grantedAuthorities);
        }

        public AuthenticatedUser withAuthorities(String... authorities) {
            for (String a : authorities) {
                if (!grantedAuthorities.contains(a)) {
                    grantedAuthorities.add(a);
                }
            }

            return this;
        }

    }

}
