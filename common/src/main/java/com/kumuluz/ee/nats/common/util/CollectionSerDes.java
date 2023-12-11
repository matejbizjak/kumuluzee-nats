package com.kumuluz.ee.nats.common.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Util class for de/serializing Java Collection types (choosing the correct parameter/return type).
 *
 * @author Matej Bizjak
 */

public class CollectionSerDes {

    public static JavaType getCollectionParameterType(Method method) {
        TypeFactory typeFactory = SerDes.getTypeFactory();
        Class<?> parameterType = method.getParameterTypes()[0];
        JavaType deserType;
        ParameterizedType genericParameterType;
        if (Map.class.isAssignableFrom(parameterType)) {
            genericParameterType = (ParameterizedType) method.getGenericParameterTypes()[0];
            deserType = typeFactory.constructMapType(Map.class, typeFactory.constructType(genericParameterType.getActualTypeArguments()[0])
                    , typeFactory.constructType(genericParameterType.getActualTypeArguments()[1]));
        } else if (Set.class.isAssignableFrom(parameterType)) {
            genericParameterType = (ParameterizedType) method.getGenericParameterTypes()[0];
            deserType = typeFactory.constructCollectionType(Set.class, typeFactory.constructType(genericParameterType.getActualTypeArguments()[0]));
        } else if (List.class.isAssignableFrom(parameterType)) {
            genericParameterType = (ParameterizedType) method.getGenericParameterTypes()[0];
            deserType = typeFactory.constructCollectionType(List.class, typeFactory.constructType(genericParameterType.getActualTypeArguments()[0]));
        } else {
            deserType = typeFactory.constructType(parameterType);
        }
        return deserType;
    }

    public static JavaType getCollectionReturnType(Method method) {
        TypeFactory typeFactory = SerDes.getTypeFactory();
        JavaType deserType;

        Class<?> returnType = method.getReturnType();

        // if CompletableFuture, we need to work with its generic type parameter
        if (returnType.isAssignableFrom(CompletableFuture.class)) {
            ParameterizedType genericReturnType = (ParameterizedType) method.getGenericReturnType();
            Type[] typeArguments = genericReturnType.getActualTypeArguments();
            if (typeArguments.length > 0) {
                Type typeArgument = typeArguments[0];
                if (typeArgument instanceof Class<?>) {
                    returnType = (Class<?>) typeArgument;
                }
            }
        }

        if (Map.class.isAssignableFrom(returnType)) {
            ParameterizedType genericParameterType = (ParameterizedType) method.getGenericReturnType();
            deserType = typeFactory.constructMapType(Map.class, typeFactory.constructType(genericParameterType.getActualTypeArguments()[0])
                    , typeFactory.constructType(genericParameterType.getActualTypeArguments()[1]));
        } else if (Set.class.isAssignableFrom(returnType)) {
            ParameterizedType genericParameterType = (ParameterizedType) method.getGenericReturnType();
            deserType = typeFactory.constructCollectionType(Set.class, typeFactory.constructType(genericParameterType.getActualTypeArguments()[0]));
        } else if (List.class.isAssignableFrom(returnType)) {
            ParameterizedType genericParameterType = (ParameterizedType) method.getGenericReturnType();
            deserType = typeFactory.constructCollectionType(List.class, typeFactory.constructType(genericParameterType.getActualTypeArguments()[0]));
        } else {
            deserType = typeFactory.constructType(returnType);
        }
        return deserType;
    }
}
