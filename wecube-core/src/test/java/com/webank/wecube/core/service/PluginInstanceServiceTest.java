package com.webank.wecube.core.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class PluginInstanceServiceTest {

    @Autowired
    PluginInstanceService pluginInstanceService;

    @Test
    public void isIpValidityTestCase1() {
        String ip = "0.0.0.0";
        log.info("Checking IP: " + ip);
        assertThat(pluginInstanceService.isIpValidity(ip)).isFalse();
    }

    @Test
    public void isIpValidityTestCase2() {
        String ip = "255.256.255.255";
        log.info("Checking IP: " + ip);
        assertThat(pluginInstanceService.isIpValidity(ip)).isFalse();
    }

    @Test
    public void isIpValidityTestCase3() {
        String ip = "019.0.0.1";
        log.info("Checking IP: " + ip);
        assertThat(pluginInstanceService.isIpValidity(ip)).isFalse();
    }

    @Test
    public void isIpValidityTestCase4() {
        String ip = "211212";
        log.info("Checking IP: " + ip);
        assertThat(pluginInstanceService.isIpValidity(ip)).isFalse();
    }

    @Test
    public void isIpValidityTestCase5() {
        String ip = "192.0.0.1";
        log.info("Checking IP: " + ip);
        assertThat(pluginInstanceService.isIpValidity(ip)).isTrue();
    }

    @Test
    public void isIpValidityTestCase6() {
        String ip = "localhost";
        log.info("Checking IP: " + ip);
        assertThat(pluginInstanceService.isIpValidity(ip)).isTrue();
    }

    @Test
    public void isIpValidityTestCase7() {
        String ip = "***REMOVED***";
        log.info("Checking IP: " + ip);
        assertThat(pluginInstanceService.isIpValidity(ip)).isTrue();
    }

}
