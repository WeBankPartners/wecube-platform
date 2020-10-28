package com.webank.wecube.platform.auth.server.service;

import java.util.Random;

public class PasswordGenerator {
    
    private static final char[] RAN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final int DEF_LENGTH = 6;
    
    private Random random = new Random();
    
    public String randomPassword(int length) {
        if(length < 0 || length > 16) {
            length = DEF_LENGTH;
        }
        
        char [] ranChars = new char[length];
        for(int i = 0; i < length; i++) {
            int ranIndex = random.nextInt(RAN_CHARS.length);
            ranChars[i] = RAN_CHARS[ranIndex];
        }
        return new String(ranChars);
    }

}
