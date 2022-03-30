package com.kumuluz.ee.nats.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author Matej Bizjak
 */

public class SerDes {
    static ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public static byte[] serialize(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(object);
    }

    public static <T> T deserialize(byte[] data, Class<T> clazz) throws IOException {
        return objectMapper.readValue(data, clazz);
    }
}
