package com.webank.wecube.platform.core.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

    public static String toJsonString(Object object){
        if(object == null) return "";

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return String.valueOf(object);
        }
    }

    public static <T> List<T> toList(String jsonContent, Class<T> clzz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaType javaType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, clzz);
        return (List<T>) mapper.readValue(jsonContent.getBytes(), javaType);
    }

    public static <T> T toObject(String jsonContent, Class<T> clzz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaType javaType = mapper.getTypeFactory().constructType(clzz);
        return mapper.readValue(jsonContent.getBytes(), javaType);
    }
}
