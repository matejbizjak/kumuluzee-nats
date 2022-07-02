package com.kumuluz.ee.nats.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kumuluz.ee.nats.common.exception.DefinitionException;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Provides {@link ObjectMapper} for the SerDes.
 *
 * @author Matej Bizjak
 */

public interface NatsObjectMapperProvider {

    /**
     * Supplies the {@link ObjectMapper} for use in SerDes. If method returns null, this
     * provider is ignored. If no providers are found, the default {@link ObjectMapper} is used.
     *
     * @return {@link ObjectMapper} that should be used or null if not applicable
     */

    ObjectMapper provideObjectMapper();

    static ObjectMapper getObjectMapper() {

        List<ObjectMapper> objectMappers = new ArrayList<>();

        ServiceLoader.load(NatsObjectMapperProvider.class).forEach(provider -> {
            ObjectMapper objectMapper = provider.provideObjectMapper();
            if (objectMapper != null) {
                objectMappers.add(objectMapper);
            }
        });

        if (objectMappers.size() == 0) {
            return new ObjectMapper().findAndRegisterModules();
        } else if (objectMappers.size() == 1) {
            return objectMappers.get(0);
        } else {
            throw new DefinitionException("Multiple ObjectMapper providers found.");
        }
    }
}
