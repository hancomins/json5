package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import java.lang.reflect.*;
import java.util.*;

/**
 * 제네릭 타입과 추상 타입/인터페이스를 처리하는 TypeHandler입니다.
 * 
 * <p>이 핸들러는 다음 타입들을 처리합니다:</p>
 * <ul>
 *   <li>제네릭 타입 (Types.GenericType)</li>
 *   <li>추상 클래스와 인터페이스 (Types.AbstractObject)</li>
 *   <li>@ObtainTypeValue 어노테이션을 통한 동적 타입 결정</li>
 * </ul>
 * 
 * <p>이 핸들러는 낮은 우선순위를 가지므로 다른 구체적인 핸들러가 먼저 시도됩니다.</p>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class GenericTypeHandler implements TypeHandler {
    
    @Override
    public boolean canHandle(Types type, Class<?> clazz) {
        return type == Types.GenericType || 
               type == Types.AbstractObject ||
               (clazz != null && (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())));
    }
    
    @Override
    public Object handleSerialization(Object value, SerializationContext context) throws SerializationException {
        if (value == null) {
            return null;
        }
        
        // 실제 객체의 타입으로 직렬화
        Class<?> actualClass = value.getClass();
        Types actualType = Types.of(actualClass);
        
        // 적절한 TypeHandler 찾기
        TypeHandler handler = context.getTypeHandlerRegistry().getHandler(actualType, actualClass);
        if (handler != null && handler != this) { // 자기 자신은 제외
            return handler.handleSerialization(value, context);
        }
        
        // Fallback: 기본 직렬화
        return JSON5Serializer.toJSON5Object(value);
    }
    
    @Override
    public Object handleDeserialization(Object element, Class<?> targetType, 
                                      DeserializationContext context) throws DeserializationException {
        if (element == null) {
            return null;
        }
        
        // @ObtainTypeValue 어노테이션을 통한 동적 타입 결정 시도
        Object resolvedObject = resolveGenericType(element, targetType, context);
        if (resolvedObject != null) {
            return resolvedObject;
        }
        
        // 인터페이스나 추상 클래스의 경우 구체 구현체 생성 시도
        Class<?> concreteType = resolveConcreteType(targetType);
        if (concreteType != null && concreteType != targetType) {
            // 구체 타입으로 역직렬화
            Types concreteTypes = Types.of(concreteType);
            TypeHandler handler = context.getTypeHandlerRegistry().getHandler(concreteTypes, concreteType);
            if (handler != null && handler != this) {
                return handler.handleDeserialization(element, concreteType, context);
            }
        }
        
        // Fallback: 기본 역직렬화 시도
        if (element instanceof JSON5Object && !targetType.isInterface() && !Modifier.isAbstract(targetType.getModifiers())) {
            return JSON5Serializer.fromJSON5Object((JSON5Object) element, targetType);
        }
        
        throw new DeserializationException("Cannot deserialize to abstract type or interface: " + targetType.getName() + 
                                         ". Consider using @ObtainTypeValue annotation.");
    }
    
    @Override
    public TypeHandlerPriority getPriority() {
        return TypeHandlerPriority.LOW; // 제네릭/추상 타입은 낮은 우선순위 (다른 핸들러 우선)
    }
    
    /**
     * @ObtainTypeValue 어노테이션을 통한 동적 타입 결정을 시도합니다.
     */
    private Object resolveGenericType(Object element, Class<?> targetType, DeserializationContext context) {
        // 실제 구현은 기존 JSON5Serializer의 동적 타입 결정 로직 활용
        // 현재는 간단한 fallback만 제공
        return null;
    }
    
    /**
     * 추상 타입이나 인터페이스에 대한 구체 구현체를 결정합니다.
     */
    private Class<?> resolveConcreteType(Class<?> abstractType) {
        // 일반적인 인터페이스들에 대한 기본 구현체 매핑
        if (abstractType == List.class) {
            return ArrayList.class;
        } else if (abstractType == Set.class) {
            return HashSet.class;
        } else if (abstractType == Map.class) {
            return HashMap.class;
        } else if (abstractType == Queue.class) {
            return ArrayDeque.class;
        } else if (abstractType == Deque.class) {
            return ArrayDeque.class;
        }
        
        // 추상 클래스나 다른 인터페이스는 현재 지원하지 않음
        return null;
    }
    
    /**
     * 제네릭 타입 정보를 해석합니다.
     */
    public boolean isGenericType(Type type, Set<String> genericTypeNames) {
        if (type instanceof TypeVariable) {
            TypeVariable<?> typeVar = (TypeVariable<?>) type;
            return genericTypeNames != null && genericTypeNames.contains(typeVar.getName());
        }
        
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            for (Type argType : paramType.getActualTypeArguments()) {
                if (isGenericType(argType, genericTypeNames)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * TypeVariable을 실제 타입으로 해석합니다.
     */
    public Class<?> resolveTypeVariable(TypeVariable<?> typeVariable, Class<?> implementationClass) {
        // 기본 구현: Object 타입으로 처리
        // 실제로는 더 복잡한 제네릭 타입 해석이 필요하지만,
        // 기존 시스템과의 호환성을 위해 단순화
        return Object.class;
    }
}
