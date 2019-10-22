package com.webank.wecube.platform.auth.client.model;

public class JwtTokenImpl {

    private String token;
    private String tokenType;
    private long expiration;

    public JwtTokenImpl() {
        super();
    }

    public JwtTokenImpl(String token, String tokenType, long expiration) {
        super();
        this.token = token;
        this.tokenType = tokenType;
        this.expiration = expiration;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (expiration ^ (expiration >>> 32));
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
        JwtTokenImpl other = (JwtTokenImpl) obj;
        if (expiration != other.expiration)
            return false;
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

}
