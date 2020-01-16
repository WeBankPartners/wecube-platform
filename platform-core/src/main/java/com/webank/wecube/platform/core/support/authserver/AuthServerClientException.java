package com.webank.wecube.platform.core.support.authserver;

import com.webank.wecube.platform.core.support.RestClientException;

public class AuthServerClientException extends RestClientException {
    /**
     * 
     */
    private static final long serialVersionUID = -8340357789825300294L;

    public AuthServerClientException() {
        super();
    }

    public AuthServerClientException(String errorStatus, String errorMessage, Throwable cause) {
        super(errorStatus, errorMessage, cause);
    }

    public AuthServerClientException(String errorStatus) {
        super(errorStatus);
    }
    public AuthServerClientException(String errorStatus,String errorMessage) {
        super(errorStatus,errorMessage);
    }

    public AuthServerClientException(Throwable cause) {
        super(cause);
    }

}
