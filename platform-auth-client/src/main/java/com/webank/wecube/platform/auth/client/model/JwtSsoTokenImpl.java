package com.webank.wecube.platform.auth.client.model;

import java.util.Date;

/**
 * 
 * @author gavin
 *
 */
abstract class JwtSsoTokenImpl implements JwtSsoToken {

    private final String token;
    private final String tokenType;
    private final Date expiration;

    protected JwtSsoTokenImpl(String token, String tokenType, long expiration) {
        super();
        this.token = token;
        this.tokenType = tokenType;
        this.expiration = new Date(expiration);
    }

    protected JwtSsoTokenImpl(String token, String tokenType, Date expiration) {
        super();
        this.token = token;
        this.tokenType = tokenType;
        this.expiration = expiration;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Date getExpiration() {
        return expiration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        result = prime * result + ((tokenType == null) ? 0 : tokenType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JwtSsoTokenImpl other = (JwtSsoTokenImpl) obj;
        if (token == null) {
            if (other.token != null)
                return false;
        } else if (!token.equals(other.token))
            return false;
        if (tokenType == null) {
            if (other.tokenType != null)
                return false;
        } else if (!tokenType.equals(other.tokenType))
            return false;
        return true;
    }

    @Override
    public Date getExpireTime() {
        return this.expiration;
    }

    @Override
    public boolean isExpired() {
        return this.expiration.before(new Date());
    }

}
