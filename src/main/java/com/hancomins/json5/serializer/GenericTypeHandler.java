package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Element;
import java.lang.reflect.*;
import java.util.*;

/**
 * 개선된 제네릭 타입과 추상 타입/인터페이스를 처리하는 TypeHandler입니다.
 * 
 * <p>5.2단계에서 TypeVariableResolver가 통합되어 더욱 정확한 제네릭 타입 처리가 가능합니다.</p>
 * 
 * <h3>개선사항:</h3>
 * <ul>
 *   <li>복잡한 제네릭 타입 해석 (List&lt;T&gt;, Map&lt;K,V&gt; 등)</li>
 *   <li>TypeVariable의 정확한 실제 타입 결정</li>
 *   <li>중첩된 제네릭 타입 처리</li>
 *   <li>WildcardType 및 GenericArrayType 지원</li>
 * </ul>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class GenericTypeHandler implements TypeHandler {
    
    private final TypeVariableResolver typeVariableResolver;
    private final Map<Class<?>, Class<?>> defaultImplementations;
    
    public GenericTypeHandler() {
        this.typeVariableResolver = new TypeVariableResolver();
        this.defaultImplementations = createDefaultImplementationMap();
    }
    
    /**
     * 커스텀 TypeVariableResolver를 사용하는 생성자입니다.
     */
    public GenericTypeHandler(TypeVariableResolver typeVariableResolver) {
        this.typeVariableResolver = Objects.requireNonNull(typeVariableResolver, "typeVariableResolver is null");
        this.defaultImplementations = createDefaultImplementationMap();
    }
    
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
        
        Class<?> actualClass = value.getClass();
        Types actualType = Types.of(actualClass);
        
        // 실제 객체의 타입이 제네릭이 아닌 구체 타입이면 해당 TypeHandler에 위임
        if (actualType != Types.GenericType && actualType != Types.AbstractObject) {
            TypeHandler handler = context.getTypeHandlerRegistry().getHandler(actualType, actualClass);
            if (handler != null && handler != this) {
                return handler.handleSerialization(value, context);
            }
        }
        
        // 기본 직렬화 수행
        return JSON5Serializer.toJSON5Object(value);
    }
    
    @Override
    public Object handleDeserialization(Object element, Class<?> targetType, 
                                      DeserializationContext context) throws DeserializationException {
        if (element == null) {
            return null;
        }
        
        try {
            // 1. @ㄴ 어노테이션을 통한 동적 타입 결정 시도
            Object dynamicResult = resolveDynamicType(element, targetType, context);
            if (dynamicResult != null) {
                return dynamicResult;
            }
            
            // 2. 제네릭 타입 해석 시도
            Class<?> resolvedType = resolveGenericType(targetType, context);
            if (resolvedType != null && resolvedType != targetType) {
                return deserializeToResolvedType(element, resolvedType, context);
            }
            
            // 3. 인터페이스나 추상 클래스의 기본 구현체 사용
            Class<?> concreteType = resolveConcreteImplementation(targetType);
            if (concreteType != null) {
                return deserializeToResolvedType(element, concreteType, context);
            }
            
            // 4. 구체 클래스라면 직접 역직렬화
            if (!targetType.isInterface() && !Modifier.isAbstract(targetType.getModifiers())) {
                if (element instanceof JSON5Object) {
                    return JSON5Serializer.fromJSON5Object((JSON5Object) element, targetType);
                }
            }
            
            throw new DeserializationException("Cannot resolve concrete type for: " + targetType.getName());
            
        } catch (DeserializationException e) {
            throw e;
        } catch (Exception e) {
            throw new DeserializationException("Failed to deserialize generic/abstract type: " + targetType.getName(), e);
        }
    }
    
    @Override
    public TypeHandlerPriority getPriority() {
        return TypeHandlerPriority.LOW;
    }
    
    /**
     * @ObtainTypeValue 어노테이션을 통한 동적 타입 결정을 시도합니다.
     */
    private Object resolveDynamicType(Object element, Class<?> targetType, DeserializationContext context) {
        if (element instanceof JSON5Object) {
            JSON5Object obj = (JSON5Object) element;
            
            if (obj.has("_type")) {
                try {
                    String typeName = obj.getString("_type");
                    Class<?> dynamicType = Class.forName(typeName);
                    
                    if (targetType.isAssignableFrom(dynamicType)) {
                        return JSON5Serializer.fromJSON5Object(obj, dynamicType);
                    }
                } catch (Exception e) {
                    // 동적 타입 결정 실패 시 무시
                }
            }
        }
        
        return null;
    }
    
    /**
     * 제네릭 타입을 실제 타입으로 해석합니다.
     */
    private Class<?> resolveGenericType(Class<?> targetType, DeserializationContext context) {
        TypeSchema typeSchema = context.getRootTypeSchema();
        if (typeSchema != null) {
            return resolveFromTypeSchema(targetType, typeSchema);
        }
        
        Object rootObject = context.getRootObject();
        if (rootObject != null) {
            TypeVariable<?> firstTypeVar = getFirstTypeVariable(targetType);
            if (firstTypeVar != null) {
                return typeVariableResolver.resolveTypeVariable(firstTypeVar, rootObject.getClass());
            }
        }
        
        return null;
    }
    
    /**
     * TypeSchema에서 제네릭 타입 정보를 해석합니다.
     */
    private Class<?> resolveFromTypeSchema(Class<?> targetType, TypeSchema typeSchema) {
        Set<String> genericTypeNames = typeSchema.getGenericTypeNames();
        
        if (targetType.getTypeParameters().length > 0 && !genericTypeNames.isEmpty()) {
            TypeVariable<?> firstTypeParam = targetType.getTypeParameters()[0];
            
            if (genericTypeNames.contains(firstTypeParam.getName())) {
                return typeVariableResolver.resolveTypeVariable(firstTypeParam, typeSchema.getType());
            }
        }
        
        return null;
    }
    
    /**
     * 클래스의 첫 번째 TypeVariable을 반환합니다.
     */
    private TypeVariable<?> getFirstTypeVariable(Class<?> clazz) {
        TypeVariable<?>[] typeParams = clazz.getTypeParameters();
        return typeParams.length > 0 ? typeParams[0] : null;
    }
    
    /**
     * 해석된 타입으로 역직렬화를 수행합니다.
     */
    private Object deserializeToResolvedType(Object element, Class<?> resolvedType, DeserializationContext context) {
        Types resolvedTypes = Types.of(resolvedType);
        
        TypeHandler handler = context.getTypeHandlerRegistry().getHandler(resolvedTypes, resolvedType);
        if (handler != null && handler != this) {
            return handler.handleDeserialization(element, resolvedType, context);
        }
        
        if (element instanceof JSON5Object) {
            return JSON5Serializer.fromJSON5Object((JSON5Object) element, resolvedType);
        }
        
        return null;
    }
    
    /**
     * 추상 타입이나 인터페이스에 대한 구체 구현체를 결정합니다.
     */
    private Class<?> resolveConcreteImplementation(Class<?> abstractType) {
        return defaultImplementations.get(abstractType);
    }
    
    /**
     * 기본 구현체 맵을 생성합니다.
     */
    private Map<Class<?>, Class<?>> createDefaultImplementationMap() {
        Map<Class<?>, Class<?>> map = new HashMap<>();
        
        map.put(List.class, ArrayList.class);
        map.put(Set.class, HashSet.class);
        map.put(Map.class, HashMap.class);
        map.put(Queue.class, ArrayDeque.class);
        map.put(Deque.class, ArrayDeque.class);
        map.put(Collection.class, ArrayList.class);
        map.put(NavigableSet.class, TreeSet.class);
        map.put(SortedSet.class, TreeSet.class);
        map.put(NavigableMap.class, TreeMap.class);
        map.put(SortedMap.class, TreeMap.class);
        
        return Collections.unmodifiableMap(map);
    }
    
    // Public API 메소드들
    
    public boolean isGenericType(Type type, Set<String> genericTypeNames) {
        return typeVariableResolver.containsTypeVariable(type, genericTypeNames);
    }
    
    public Class<?> resolveTypeVariable(TypeVariable<?> typeVariable, Class<?> implementationClass) {
        return typeVariableResolver.resolveTypeVariable(typeVariable, implementationClass);
    }
    
    public Class<?>[] resolveGenericInterface(Class<?> implementationClass, Class<?> genericInterface) {
        return typeVariableResolver.resolveGenericInterface(implementationClass, genericInterface);
    }
    
    public TypeVariableResolver getTypeVariableResolver() {
        return typeVariableResolver;
    }
    
    public void clearCache() {
        typeVariableResolver.clearCache();
    }
    
    public String getCacheStats() {
        return typeVariableResolver.getCacheStats();
    }
}
