package com.kumuluz.ee.nats.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;

/**
 * Helper for de/serialization.
 *
 * @author Matej Bizjak
 */

public class SerDes {
    static final ObjectMapper OBJECT_MAPPER = NatsObjectMapperProvider.getObjectMapper();

    public static byte[] serialize(Object object) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }

    public static <T> T deserialize(byte[] data, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(data, clazz);
    }

    public static <T> T deserialize(byte[] data, TypeReference<T> valueTypeRef) throws IOException {
        return OBJECT_MAPPER.readValue(data, valueTypeRef);
    }

    public static <T> T deserialize(byte[] data, JavaType valueType) throws IOException {
        return OBJECT_MAPPER.readValue(data, valueType);
    }

    public static TypeFactory getTypeFactory() {
        return OBJECT_MAPPER.getTypeFactory();
    }
}
