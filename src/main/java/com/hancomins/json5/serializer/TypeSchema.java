package com.hancomins.json5.serializer;


import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

class TypeSchema {


    protected static final TypeSchema JSON5_OBJECT;

    static {
        try {
            JSON5_OBJECT = new TypeSchema(JSON5Object.class, JSON5Object.class.getConstructor());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    protected static final TypeSchema JSON5_ARRAY;

    static {
        try {
            JSON5_ARRAY = new TypeSchema(JSON5Array.class, JSON5Array.class.getConstructor());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final boolean explicit;
    private final Class<?> type;
    private final Constructor<?> constructor;
    private final ConcurrentHashMap<String, ObtainTypeValueInvoker> fieldValueObtaiorMap = new ConcurrentHashMap<>();

    private SchemaObjectNode schema;

    private final String comment;
    private final String commentAfter;
    private final Set<String> genericTypeNames = new HashSet<>();


    protected SchemaObjectNode getSchemaObjectNode() {
        if(schema == null) {
            schema = NodePath.makeSchema(this,null);
        }
        return schema;
    }

    private static Class<?> findNoAnonymousClass(Class<?> type) {
        if(!type.isAnonymousClass()) {
            return type;
        }
        Class<?> superClass = type.getSuperclass();
        if(superClass != null && superClass != Object.class) {
            return superClass;
        }
        Class<?>[] interfaces = type.getInterfaces();
        if(interfaces != null && interfaces.length > 0) {
            Class<?> foundJSON5Interface = null;
            for(Class<?> interfaceClass : interfaces) {
                if(interfaceClass.getAnnotation(JSON5Type.class) != null) {
                    if(foundJSON5Interface != null) {
                        String allInterfaceNames = Arrays.stream(interfaces).map(Class::getName).reduce((a, b) -> a + ", " + b).orElse("");
                        throw new JSON5SerializerException("Anonymous class " + type.getName() + "(implements  " + allInterfaceNames + "), implements multiple @JSON5Type interfaces.  Only one @JSON5Type interface is allowed.");
                    }
                    foundJSON5Interface = interfaceClass;
                }
            }
            if(foundJSON5Interface != null) {
                return foundJSON5Interface;
            }
        }
        return type;
    }



    protected synchronized static TypeSchema create(Class<?> type) {
        type = findNoAnonymousClass(type);

        if(JSON5Object.class.isAssignableFrom(type)) {
            return JSON5_OBJECT;
        }
        if(JSON5Array.class.isAssignableFrom(type)) {
            return JSON5_ARRAY;
        }
        Constructor<?> constructor = null;
        try {
            constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException ignored) {}
        return new TypeSchema(type, constructor);
    }



    protected Object newInstance() {
        try {
            if(constructor == null) {
                checkConstructor(type);
                return null;
            }
            return constructor.newInstance();
        } catch (Exception e) {
            throw new JSON5SerializerException("Failed to create instance of " + type.getName(), e);
        }
    }



    protected boolean containsGenericType(String name) {
        return genericTypeNames.contains(name);
    }

    private TypeSchema(Class<?> type, Constructor<?> constructor) {
        this.type = type;
        this.constructor = constructor;
        JSON5Type JSON5Type = type.getAnnotation(JSON5Type.class);

        if(JSON5Type != null) {
            explicit = JSON5Type.explicit();
            String commentBefore = JSON5Type.comment();
            String commentAfter = JSON5Type.commentAfter();

            this.comment = commentBefore.isEmpty() ? null : commentBefore;
            this.commentAfter = commentAfter.isEmpty() ? null : commentAfter;
        } else {
            explicit = false;
            this.comment = null;
            this.commentAfter = null;
        }

        searchTypeParameters();
        searchMethodOfAnnotatedWithObtainTypeValue();

    }

    private void searchTypeParameters() {
        TypeVariable<?>[] typeVariables = this.type.getTypeParameters();
        for(TypeVariable<?> typeVariable : typeVariables) {
            genericTypeNames.add(typeVariable.getName());
        }
    }


    private void searchMethodOfAnnotatedWithObtainTypeValue() {
        List<ObtainTypeValueInvoker> obtainTypeValueInvokers = ObtainTypeValueInvoker.searchObtainTypeValueInvoker(this);
        for(ObtainTypeValueInvoker obtainTypeValueInvoker : obtainTypeValueInvokers) {
            // 뒤에 있는 것일수록 부모 클래스의 것이므로 덮어쓰지 않는다.
            fieldValueObtaiorMap.putIfAbsent(obtainTypeValueInvoker.getFieldName(), obtainTypeValueInvoker);
        }

    }

    @SuppressWarnings("unchecked")


    Class<?> getType() {
        return type;
    }


    String getComment() {
        return comment;
    }

    String getCommentAfter() {
        return commentAfter;
    }



    Set<String> getGenericTypeNames() {
        return genericTypeNames;
    }




    private static void checkConstructor(Class<?> type) {
        Constructor<?> constructor = null;
        try {
            constructor = type.getDeclaredConstructor();
            if(constructor == null) {
                throw new JSON5SerializerException("Type " + type.getName() + " has no default constructor");
            }
        } catch (NoSuchMethodException e) {
            throw new JSON5SerializerException("Type " + type.getName() + " has invalid default constructor");
        }

    }

    ObtainTypeValueInvoker findObtainTypeValueInvoker(String fieldName) {
        return fieldValueObtaiorMap.get(fieldName);
    }


    boolean isExplicit() {
        return explicit;
    }



}
