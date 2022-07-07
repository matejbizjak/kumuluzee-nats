package com.kumuluz.ee.nats.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Helper for de/serialization.
 *
 * @author Matej Bizjak
 */

public class SerDes {
    static ObjectMapper objectMapper = NatsObjectMapperProvider.getObjectMapper();

    public static byte[] serialize(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(object);
    }

    public static <T> T deserialize(byte[] data, Class<T> clazz) throws IOException {
        return objectMapper.readValue(data, clazz);
    }
}
