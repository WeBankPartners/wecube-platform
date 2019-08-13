package com.webank.wecube.core.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class VersionUtilsTest {

    @Test
    public void compare() {
        assertEquals(1, VersionUtils.compare("v1", "0.9"));
        assertEquals(1, VersionUtils.compare("0.0.0.2", "0.0.0.1"));
        assertEquals(1, VersionUtils.compare("1.0", "0.9"));
        assertEquals(1, VersionUtils.compare("2.0.1", "2.0.0"));
        assertEquals(1, VersionUtils.compare("2.0.1", "2.0"));
        assertEquals(1, VersionUtils.compare("2.0.1", "V2"));
        assertEquals(1, VersionUtils.compare("0.a.1", "0.a.0"));
        assertEquals(1, VersionUtils.compare("0.9.beta", "0.9.alpha"));
        assertEquals(1, VersionUtils.compare("0.9.11", "0.9.2"));
        assertEquals(1, VersionUtils.compare("0.9.12", "0.9.11"));
        assertEquals(1, VersionUtils.compare("0.10", "0.9"));
        assertEquals(0, VersionUtils.compare("0.10-1/2-alpha", "0.10.1-2-alpha"));
        assertEquals(-1, VersionUtils.compare("2.10", "2.10.1"));
        assertEquals(-1, VersionUtils.compare("0.0.0.2", "0.1"));
        assertEquals(1, VersionUtils.compare("1.0", "0.9.2"));
        assertEquals(1, VersionUtils.compare("1.10", "1.6"));
        assertEquals(1, VersionUtils.compare("1.10.0.0.0.1", "1.10"));
        assertEquals(1, VersionUtils.compare("1.10.0.0.0.0", "1.10"));


    }
}
