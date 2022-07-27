package com.kumuluz.ee.nats.tests.beans.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kumuluz.ee.nats.common.util.NatsObjectMapperProvider;

/**
 * @author Matej Bizjak
 */

public class NatsMapperProvider implements NatsObjectMapperProvider {

    @Override
    public ObjectMapper provideObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
