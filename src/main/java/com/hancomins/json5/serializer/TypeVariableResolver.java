package com.hancomins.json5.serializer;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 제네릭 타입 정보를 해석하고 실제 타입으로 변환하는 클래스입니다.
 * 
 * <p>이 클래스는 Java의 복잡한 제네릭 타입 시스템을 분석하여
 * 런타임에 실제 타입 정보를 추출하는 기능을 제공합니다.</p>
 * 
 * <h3>지원하는 제네릭 타입:</h3>
 * <ul>
 *   <li>TypeVariable (T, E, K, V 등)</li>
 *   <li>ParameterizedType (List&lt;String&gt;, Map&lt;K,V&gt; 등)</li>
 *   <li>WildcardType (? extends Number, ? super String 등)</li>
 *   <li>GenericArrayType (T[] 등)</li>
 * </ul>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class TypeVariableResolver {
    
    /** 타입 해석 결과 캐시 (성능 최적화) */
    private final Map<String, Class<?>> resolveCache = new ConcurrentHashMap<>();
    
    /** 순환 참조 방지를 위한 현재 해석 중인 타입들 */
    private final ThreadLocal<Set<String>> resolvingTypes = ThreadLocal.withInitial(HashSet::new);
    
    /**
     * TypeVariable을 실제 타입으로 해석합니다.
     * 
     * @param typeVariable 해석할 TypeVariable
     * @param implementationClass 구현 클래스 (제네릭 타입 정보를 가지고 있음)
     * @return 해석된 실제 타입, 해석할 수 없으면 Object.class
     */
    public Class<?> resolveTypeVariable(TypeVariable<?> typeVariable, Class<?> implementationClass) {
        if (typeVariable == null || implementationClass == null) {
            return Object.class;
        }
        
        String cacheKey = createCacheKey(typeVariable, implementationClass);
        
        // 캐시에서 먼저 확인
        Class<?> cached = resolveCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 순환 참조 방지
        Set<String> currentlyResolving = resolvingTypes.get();
        if (currentlyResolving.contains(cacheKey)) {
            return Object.class;
        }
        
        try {
            currentlyResolving.add(cacheKey);
            Class<?> resolved = doResolveTypeVariable(typeVariable, implementationClass);
            
            // 캐시에 저장
            resolveCache.put(cacheKey, resolved);
            return resolved;
            
        } finally {
            currentlyResolving.remove(cacheKey);
        }
    }
    
    /**
     * 실제 TypeVariable 해석 로직을 수행합니다.
     */
    private Class<?> doResolveTypeVariable(TypeVariable<?> typeVariable, Class<?> implementationClass) {
        String typeVarName = typeVariable.getName();
        
        // 1. 클래스 계층구조에서 제네릭 타입 정보 찾기
        Class<?> resolved = resolveFromClassHierarchy(typeVarName, implementationClass);
        if (resolved != null && resolved != Object.class) {
            return resolved;
        }
        
        // 2. 상위 클래스에서 찾기
        resolved = resolveFromSuperclass(typeVarName, implementationClass);
        if (resolved != null && resolved != Object.class) {
            return resolved;
        }
        
        // 3. 인터페이스에서 찾기
        resolved = resolveFromInterfaces(typeVarName, implementationClass);
        if (resolved != null && resolved != Object.class) {
            return resolved;
        }
        
        // 4. TypeVariable의 bounds에서 추론
        resolved = resolveFromBounds(typeVariable);
        if (resolved != null && resolved != Object.class) {
            return resolved;
        }
        
        // 5. 해석할 수 없으면 Object 반환
        return Object.class;
    }
    
    /**
     * 클래스 계층구조에서 제네릭 타입 정보를 찾습니다.
     */
    private Class<?> resolveFromClassHierarchy(String typeVarName, Class<?> clazz) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericSuperclass;
            Class<?> rawType = (Class<?>) paramType.getRawType();
            
            // 상위 클래스의 TypeParameter와 매칭
            TypeVariable<?>[] typeParams = rawType.getTypeParameters();
            Type[] actualTypes = paramType.getActualTypeArguments();
            
            for (int i = 0; i < typeParams.length && i < actualTypes.length; i++) {
                if (typeParams[i].getName().equals(typeVarName)) {
                    return resolveActualType(actualTypes[i], clazz);
                }
            }
        }
        
        return null;
    }
    
    /**
     * 상위 클래스에서 제네릭 타입 정보를 찾습니다.
     */
    private Class<?> resolveFromSuperclass(String typeVarName, Class<?> clazz) {
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            return resolveFromClassHierarchy(typeVarName, superclass);
        }
        return null;
    }
    
    /**
     * 인터페이스에서 제네릭 타입 정보를 찾습니다.
     */
    private Class<?> resolveFromInterfaces(String typeVarName, Class<?> clazz) {
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericInterface;
                Class<?> rawType = (Class<?>) paramType.getRawType();
                
                TypeVariable<?>[] typeParams = rawType.getTypeParameters();
                Type[] actualTypes = paramType.getActualTypeArguments();
                
                for (int i = 0; i < typeParams.length && i < actualTypes.length; i++) {
                    if (typeParams[i].getName().equals(typeVarName)) {
                        return resolveActualType(actualTypes[i], clazz);
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * TypeVariable의 bounds에서 타입을 추론합니다.
     */
    private Class<?> resolveFromBounds(TypeVariable<?> typeVariable) {
        Type[] bounds = typeVariable.getBounds();
        
        if (bounds.length > 0) {
            Type bound = bounds[0]; // 첫 번째 bound 사용
            
            if (bound instanceof Class) {
                return (Class<?>) bound;
            } else if (bound instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) bound).getRawType();
            }
        }
        
        return Object.class;
    }
    
    /**
     * 실제 타입 인수를 해석합니다.
     */
    public Class<?> resolveActualType(Type actualType, Class<?> context) {
        if (actualType instanceof Class) {
            return (Class<?>) actualType;
        } else if (actualType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) actualType).getRawType();
        } else if (actualType instanceof TypeVariable) {
            // 재귀적으로 해석
            return resolveTypeVariable((TypeVariable<?>) actualType, context);
        } else if (actualType instanceof WildcardType) {
            return resolveWildcardType((WildcardType) actualType, context);
        } else if (actualType instanceof GenericArrayType) {
            return resolveGenericArrayType((GenericArrayType) actualType, context);
        }
        
        return Object.class;
    }
    
    /**
     * WildcardType을 해석합니다 (? extends/super).
     */
    private Class<?> resolveWildcardType(WildcardType wildcardType, Class<?> context) {
        Type[] upperBounds = wildcardType.getUpperBounds();
        if (upperBounds.length > 0) {
            return resolveActualType(upperBounds[0], context);
        }
        
        Type[] lowerBounds = wildcardType.getLowerBounds();
        if (lowerBounds.length > 0) {
            return resolveActualType(lowerBounds[0], context);
        }
        
        return Object.class;
    }
    
    /**
     * GenericArrayType을 해석합니다 (T[]).
     */
    private Class<?> resolveGenericArrayType(GenericArrayType genericArrayType, Class<?> context) {
        Type componentType = genericArrayType.getGenericComponentType();
        Class<?> componentClass = resolveActualType(componentType, context);
        
        try {
            // 배열 클래스 생성 (예: String[] -> String[].class)
            return Array.newInstance(componentClass, 0).getClass();
        } catch (Exception e) {
            return Object[].class; // Fallback
        }
    }
    
    /**
     * 제네릭 타입이 특정 타입 변수 이름들을 포함하는지 확인합니다.
     */
    public boolean containsTypeVariable(Type type, Set<String> typeVarNames) {
        if (type instanceof TypeVariable) {
            TypeVariable<?> typeVar = (TypeVariable<?>) type;
            return typeVarNames.contains(typeVar.getName());
        } else if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            for (Type argType : paramType.getActualTypeArguments()) {
                if (containsTypeVariable(argType, typeVarNames)) {
                    return true;
                }
            }
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            return containsTypeVariable(arrayType.getGenericComponentType(), typeVarNames);
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            
            for (Type bound : wildcardType.getUpperBounds()) {
                if (containsTypeVariable(bound, typeVarNames)) {
                    return true;
                }
            }
            
            for (Type bound : wildcardType.getLowerBounds()) {
                if (containsTypeVariable(bound, typeVarNames)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 제네릭 타입의 실제 타입 인수들을 해석합니다.
     * 
     * @param parameterizedType 매개변수화된 타입
     * @param context 해석 컨텍스트 클래스
     * @return 해석된 타입 인수들
     */
    public Class<?>[] resolveTypeArguments(ParameterizedType parameterizedType, Class<?> context) {
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        Class<?>[] resolved = new Class<?>[actualTypeArguments.length];
        
        for (int i = 0; i < actualTypeArguments.length; i++) {
            resolved[i] = resolveActualType(actualTypeArguments[i], context);
        }
        
        return resolved;
    }
    
    /**
     * 클래스가 특정 제네릭 인터페이스를 구현하는지 확인하고 타입 인수를 해석합니다.
     * 
     * @param implementationClass 구현 클래스
     * @param genericInterface 제네릭 인터페이스 (예: List.class)
     * @return 해석된 타입 인수들, 구현하지 않으면 null
     */
    public Class<?>[] resolveGenericInterface(Class<?> implementationClass, Class<?> genericInterface) {
        Type[] genericInterfaces = implementationClass.getGenericInterfaces();
        
        for (Type type : genericInterfaces) {
            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                if (paramType.getRawType() == genericInterface) {
                    return resolveTypeArguments(paramType, implementationClass);
                }
            }
        }
        
        // 상위 클래스에서 확인
        Class<?> superclass = implementationClass.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            return resolveGenericInterface(superclass, genericInterface);
        }
        
        return null;
    }
    
    /**
     * 캐시 키를 생성합니다.
     */
    private String createCacheKey(TypeVariable<?> typeVariable, Class<?> implementationClass) {
        return typeVariable.getName() + "@" + 
               typeVariable.getGenericDeclaration().toString() + "#" + 
               implementationClass.getName();
    }
    
    /**
     * 캐시를 초기화합니다.
     */
    public void clearCache() {
        resolveCache.clear();
    }
    
    /**
     * 현재 캐시 크기를 반환합니다.
     */
    public int getCacheSize() {
        return resolveCache.size();
    }
    
    /**
     * 캐시 통계를 문자열로 반환합니다.
     */
    public String getCacheStats() {
        return "TypeVariableResolver Cache: " + resolveCache.size() + " entries";
    }
}
