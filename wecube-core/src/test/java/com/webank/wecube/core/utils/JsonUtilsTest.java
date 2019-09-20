package com.webank.wecube.core.utils;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

public class JsonUtilsTest {
    @Test
    public void whenConvertJsonToMapShouldBeSuccess() throws IOException {
        Map<?, ?> map = JsonUtils.toObject("{\"username\":\"wecube\",\"password\":\"123456\"}", Map.class);
        assertEquals(map.get("username"), "wecube");
    }
}
