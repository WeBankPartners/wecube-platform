package com.webank.wecube.platform.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindVarNameUtilsTest {
    private static final Logger log = LoggerFactory.getLogger(FindVarNameUtilsTest.class);

    @Test
    public void findVarNameTestCase1() {
        String str = "{{ALLOCATE_PORT}}:21000";
        log.info("Checking str: " + str);
        List<String> stringList = StringUtils.findSystemVariableString(str);
        log.info("Checking stringList: " + stringList);

        assertThat(stringList).contains("{{ALLOCATE_PORT}}");
    }

    @Test
    public void findVarNameTestCase2() {
        String str = "{{BASE_MOUNT_PATH}}/service-mgmt/log:/log";
        log.info("Checking str: " + str);
        assertThat(StringUtils.findSystemVariableString(str)).contains("{{BASE_MOUNT_PATH}}");
    }

    @Test
    public void findVarNameTestCase3() {
        String str = "DB_HOST={{DB_HOST}},DB_PORT={{DB_PORT}},DB_SCHEMA={{DB_SCHEMA}},DB_USER={{DB_USER}},DB_PWD={{DB_PWD}},CORE_ADDR={{CORE_ADDR}}";
        log.info("Checking str: " + str);
        assertThat(StringUtils.findSystemVariableString(str)).contains("{{DB_HOST}}", "{{DB_PORT}}", "{{DB_SCHEMA}}", "{{DB_USER}}",
                "{{DB_PWD}}", "{{CORE_ADDR}}");
    }

}
