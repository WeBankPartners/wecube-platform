package com.webank.wecube.platform.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StringUtilsTest {

    @Test
    public void isValidIpTestCase1() {
        String ip = "0.0.0.0";
        log.info("Checking IP: " + ip);
        assertThat(StringUtils.isValidIp(ip)).isFalse();
    }

    @Test
    public void isValidIpTestCase2() {
        String ip = "255.256.255.255";
        log.info("Checking IP: " + ip);
        assertThat(StringUtils.isValidIp(ip)).isFalse();
    }

    @Test
    public void isValidIpTestCase3() {
        String ip = "019.0.0.1";
        log.info("Checking IP: " + ip);
        assertThat(StringUtils.isValidIp(ip)).isFalse();
    }

    @Test
    public void isValidIpTestCase4() {
        String ip = "211212";
        log.info("Checking IP: " + ip);
        assertThat(StringUtils.isValidIp(ip)).isFalse();
    }

    @Test
    public void isValidIpTestCase5() {
        String ip = "192.0.0.1";
        log.info("Checking IP: " + ip);
        assertThat(StringUtils.isValidIp(ip)).isTrue();
    }

    @Test
    public void isValidIpTestCase6() {
        String ip = "localhost";
        log.info("Checking IP: " + ip);
        assertThat(StringUtils.isValidIp(ip)).isTrue();
    }

    @Test
    public void isValidIpTestCase7() {
        String ip = "***REMOVED***";
        log.info("Checking IP: " + ip);
        assertThat(StringUtils.isValidIp(ip)).isTrue();
    }

}
