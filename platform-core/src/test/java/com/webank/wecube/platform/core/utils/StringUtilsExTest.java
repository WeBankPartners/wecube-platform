package com.webank.wecube.platform.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

@Ignore
public class StringUtilsExTest {
    private static final Logger log = LoggerFactory.getLogger(StringUtilsExTest.class);

    @Test
    public void isValidIpTestCase1() {
        String ip = "0.0.0.0";
        log.info("Checking IP: " + ip);
        assertThat(StringUtilsEx.isValidIp(ip)).isFalse();
    }

    @Test
    public void isValidIpTestCase2() {
        String ip = "255.256.255.255";
        log.info("Checking IP: " + ip);
        assertThat(StringUtilsEx.isValidIp(ip)).isFalse();
    }

    @Test
    public void isValidIpTestCase3() {
        String ip = "019.0.0.1";
        log.info("Checking IP: " + ip);
        assertThat(StringUtilsEx.isValidIp(ip)).isFalse();
    }

    @Test
    public void isValidIpTestCase4() {
        String ip = "211212";
        log.info("Checking IP: " + ip);
        assertThat(StringUtilsEx.isValidIp(ip)).isFalse();
    }

    @Test
    public void isValidIpTestCase5() {
        String ip = "192.0.0.1";
        log.info("Checking IP: " + ip);
        assertThat(StringUtilsEx.isValidIp(ip)).isTrue();
    }

    @Test
    public void isValidIpTestCase6() {
        String ip = "127.10.10.10";
        log.info("Checking IP: " + ip);
        assertThat(StringUtilsEx.isValidIp(ip)).isTrue();
    }

    @Test
    public void isValidIpTestCase7() {
        String ip = "196.0.0.72";
        log.info("Checking IP: " + ip);
        assertThat(StringUtilsEx.isValidIp(ip)).isTrue();
    }

    @Test
    public void splitIpTest() {
        String str1 = "127.0.0.1,127.0.0.2";
        String str2 = "127.0.0.1,";
        String str3 = ",127.0.0.1";
        String str4 = "127.0.0.1";
        String str5 = "";
        
        assertThat(StringUtilsEx.splitByComma(str1).equals(Lists.newArrayList("127.0.0.1","127.0.0.2")));
        assertThat(StringUtilsEx.splitByComma(str2).equals(Lists.newArrayList("127.0.0.1")));
        assertThat(StringUtilsEx.splitByComma(str3).equals(Lists.newArrayList("127.0.0.1")));
        assertThat(StringUtilsEx.splitByComma(str4).equals(Lists.newArrayList("127.0.0.1")));
        assertThat(StringUtilsEx.splitByComma(str5).equals(Lists.newArrayList()));
        assertThat(StringUtilsEx.splitByComma(str5).size()==0);
    }

}
