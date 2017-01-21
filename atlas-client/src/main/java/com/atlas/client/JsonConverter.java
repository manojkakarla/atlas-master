package com.atlas.client;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.util.StringUtils.isEmpty;


@Slf4j
public class JsonConverter implements Serializable {

    private ObjectMapper mapper;

    public JsonConverter(ObjectMapper objectMapper) {
        mapper = objectMapper;
    }

    public <T> T parseFromJson(String jsonString, Class<T> tClass, Class<?>... types){
        if(isEmpty(jsonString)) {
            return null;
        }

        return parseFromJson(new ByteArrayInputStream(jsonString.getBytes()), tClass, types);
    }


    public <T> T parseFromJson(InputStream jsonStream, Class<T> tClass, Class<?>... types) {

        T result = null;
        try {
            if(types == null || types.length == 0){
                result = mapper.readValue(jsonStream, tClass);
            } else {
                JavaType type = mapper.getTypeFactory().constructParametricType(tClass, types);
                result = mapper.readValue(jsonStream, type);
            }
        } catch (IOException e) {
            log.error(String.format("%s parsing json string : {%s}" , e.getMessage(), jsonStream), e);
        }
        return result;
    }

    public String convertToJson(Object obj) {
        if(null == obj) {
            return null;
        }
        String result = null;
        try {
            result = mapper.writeValueAsString(obj);
        } catch (IOException e) {
            log.error(String.format("%s Converting json string : {%s}" , e.getMessage(), obj), e);
        }
        return result;
    }
}
