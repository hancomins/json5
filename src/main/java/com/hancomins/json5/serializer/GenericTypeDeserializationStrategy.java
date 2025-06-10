package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.WildcardType;

/**
 * 제네릭 타입의 역직렬화를 담당하는 전략 클래스
 * 
 * 제네릭 타입, 추상 타입, 인터페이스 등 복잡한 타입의 
 * 역직렬화를 처리합니다.
 */
public class GenericTypeDeserializationStrategy implements DeserializationStrategy {
    
    @Override
    public boolean canHandle(Types type, Class<?> targetType) {
        return type == Types.GenericType || 
               type == Types.AbstractObject ||
               targetType.isInterface() ||
               java.lang.reflect.Modifier.isAbstract(targetType.getModifiers());
    }
    
    @Override
    public Object deserialize(JSON5Element json5Element, Class<?> targetType, DeserializationContext context) {
        if (json5Element == null) {
            return null;
        }
        
        // JSON5Object인 경우에만 처리
        if (!(json5Element instanceof JSON5Object)) {
            return null;
        }
        
        JSON5Object json5Object = (JSON5Object) json5Element;
        
        // TypeHandler를 통한 처리 시도
        TypeHandlerRegistry registry = context.getTypeHandlerRegistry();
        if (registry != null) {
            TypeHandler handler = registry.getHandler(Types.of(targetType), targetType);
            if (handler != null && handler.canHandle(Types.of(targetType), targetType)) {
                try {
                    return handler.handleDeserialization(json5Element, targetType, context);
                } catch (Exception e) {
                    // TypeHandler 실패 시 기본 처리로 진행
                }
            }
        }
        
        // 제네릭 타입 해석 시도
        Object resolvedInstance = resolveGenericType(json5Object, targetType, context);
        if (resolvedInstance != null) {
            return resolvedInstance;
        }
        
        // 추상 타입/인터페이스 처리
        if (targetType.isInterface() || java.lang.reflect.Modifier.isAbstract(targetType.getModifiers())) {
            return handleAbstractType(json5Object, targetType, context);
        }
        
        return null;
    }
    
    @Override
    public int getPriority() {
        return 80; // 낮은 우선순위 (다른 전략들이 처리하지 못할 때)
    }
    
    /**
     * 제네릭 타입을 해석하여 인스턴스를 생성합니다.
     */
    private Object resolveGenericType(JSON5Object json5Object, Class<?> targetType, DeserializationContext context) {
        try {
            // TypeVariable 확인
            if (hasTypeVariables(targetType)) {
                Class<?> resolvedType = resolveTypeVariables(targetType, context);
                if (resolvedType != null && !resolvedType.equals(targetType)) {
                    ObjectDeserializer objectDeserializer = new ObjectDeserializer();
                    Object instance = createInstance(resolvedType);
                    if (instance != null) {
                        return objectDeserializer.deserialize(json5Object, instance);
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 추상 타입이나 인터페이스를 처리합니다.
     */
    private Object handleAbstractType(JSON5Object json5Object, Class<?> targetType, DeserializationContext context) {
        // 구체적인 구현 클래스 정보가 JSON에 있는지 확인
        String className = json5Object.getString("$type");
        if (className != null) {
            try {
                Class<?> concreteClass = Class.forName(className);
                if (targetType.isAssignableFrom(concreteClass)) {
                    Object instance = createInstance(concreteClass);
                    if (instance != null) {
                        ObjectDeserializer objectDeserializer = new ObjectDeserializer();
                        return objectDeserializer.deserialize(json5Object, instance);
                    }
                }
            } catch (ClassNotFoundException e) {
                // 클래스를 찾을 수 없는 경우 무시
            }
        }
        
        // 일반적인 구현체 추론 시도
        Class<?> concreteClass = inferConcreteClass(targetType);
        if (concreteClass != null) {
            try {
                Object instance = createInstance(concreteClass);
                if (instance != null) {
                    ObjectDeserializer objectDeserializer = new ObjectDeserializer();
                    return objectDeserializer.deserialize(json5Object, instance);
                }
            } catch (Exception e) {
                // 인스턴스 생성 실패 시 무시
            }
        }
        
        return null;
    }
    
    /**
     * 타입에 TypeVariable이 있는지 확인합니다.
     */
    private boolean hasTypeVariables(Class<?> type) {
        return type.getTypeParameters().length > 0;
    }
    
    /**
     * TypeVariable을 실제 타입으로 해석합니다.
     */
    private Class<?> resolveTypeVariables(Class<?> type, DeserializationContext context) {
        // 단순화된 구현: 컨텍스트에서 타입 정보를 찾거나 기본값 반환
        // 실제로는 더 복잡한 제네릭 타입 해석이 필요할 수 있음
        
        TypeSchema rootTypeSchema = context.getRootTypeSchema();
        if (rootTypeSchema != null) {
            Class<?> rootType = rootTypeSchema.getType();
            if (type.isAssignableFrom(rootType)) {
                return rootType;
            }
        }
        
        return type; // 해석 실패 시 원본 타입 반환
    }
    
    /**
     * 추상 타입에 대한 구체적인 구현 클래스를 추론합니다.
     */
    private Class<?> inferConcreteClass(Class<?> abstractType) {
        // 일반적인 인터페이스에 대한 기본 구현체 매핑
        if (abstractType == java.util.List.class) {
            return java.util.ArrayList.class;
        } else if (abstractType == java.util.Set.class) {
            return java.util.HashSet.class;
        } else if (abstractType == java.util.Map.class) {
            return java.util.HashMap.class;
        } else if (abstractType == java.util.Queue.class) {
            return java.util.LinkedList.class;
        } else if (abstractType == java.util.Deque.class) {
            return java.util.ArrayDeque.class;
        }
        
        return null; // 추론 불가능
    }
    
    /**
     * 클래스의 인스턴스를 생성합니다.
     */
    private Object createInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
