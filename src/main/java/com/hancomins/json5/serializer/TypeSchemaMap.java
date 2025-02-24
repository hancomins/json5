package com.hancomins.json5.serializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class TypeSchemaMap {

    private static final TypeSchemaMap instance = new TypeSchemaMap();

    private final Map<Class<?>, TypeSchema> typeInfoMap = new ConcurrentHashMap<>();

    private TypeSchemaMap() {
    }

    static TypeSchemaMap getInstance() {
        return instance;
    }

    boolean hasTypeInfo(Class<?> type) {
        return typeInfoMap.containsKey(type);
    }

    private Class<?> getSuperClassIfAnonymous(Class<?> type) {
        if(!type.isAnonymousClass()) {
            return type;
        }
        Class<?> superClass = type.getSuperclass();
        if(superClass != null && superClass != Object.class && type.getAnnotation(JSON5Type.class) != null) {
            return superClass;
        }
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> interfaceClass : interfaces) {
            if (interfaceClass.getAnnotation(JSON5Type.class) != null) {
                return interfaceClass;
            }
        }
        return type;
    }



    TypeSchema getTypeInfo(Class<?> type) {
        if(type.isAnonymousClass()) {
            type = getSuperClassIfAnonymous(type);
        }
        return typeInfoMap.computeIfAbsent(type, TypeSchema::create);
    }


}
