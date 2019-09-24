package com.webank.wecube.platform.auth.server.common;

public final class ApplicationConstants {
    
    public static class Profile {
        public static final String DEV = "dev";
        public static final String TEST = "test";
        public static final String PROD = "prod";
        public static final String DEFAULT = "default";
    }
    
    public static class ApiInfo {
        public static final String BASE_PACKAGE = "com.webank.wecube.platform.auth.server";
        public static final String TITLE = "Platform Auth Server API Documentation";
        public static final String VERSION_V1 = "v1";
        public static final String VERSION_V2 = "v2";
        public static final String VERSION_V3 = "v3";
        public static final String PREFIX_VERSION_V1 = "/v1/api";
        public static final String PREFIX_VERSION_V2 = "/v2/api";
        public static final String PREFIX_VERSION_V3 = "/v3/api";
        
        public static final String PREFIX_DEFAULT = PREFIX_VERSION_V1;
        
    }
}
