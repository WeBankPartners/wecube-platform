package com.webank.wecube.core.interceptor;

public class UsernameStorage extends ThreadLocal<String>{
    private static UsernameStorage instance = new UsernameStorage();
    
    public static UsernameStorage getIntance() {
        return instance;
    }
}
