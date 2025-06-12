package com.hancomins.json5.serializer.polymorphic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 서브타입 매핑 정보를 관리합니다.
 * 성능을 위해 분석 결과를 캐싱합니다.
 */
public class SubTypeRegistry {
    private final Map<Class<?>, TypeInfo> typeInfoMap = new ConcurrentHashMap<>();
    private final TypeInfoAnalyzer analyzer = new TypeInfoAnalyzer();
    
    /**
     * 타입 정보를 등록합니다.
     * @param baseType 기본 타입
     * @param typeInfo 타입 정보
     */
    public void registerTypeInfo(Class<?> baseType, TypeInfo typeInfo) {
        typeInfoMap.put(baseType, typeInfo);
    }
    
    /**
     * 타입 정보를 가져옵니다. 없으면 분석하여 캐싱합니다.
     * @param baseType 기본 타입
     * @return 타입 정보, 다형성 타입이 아니면 null
     */
    public TypeInfo getTypeInfo(Class<?> baseType) {
        if (baseType == null) {
            return null;
        }
        
        return typeInfoMap.computeIfAbsent(baseType, clazz -> {
            TypeInfo info = analyzer.analyzeTypeInfo(clazz);
            return info; // null도 캐싱하여 반복 분석 방지
        });
    }
    
    /**
     * 구체적인 타입을 해결합니다.
     * @param baseType 기본 타입
     * @param typeName 타입 이름
     * @return 구체적인 클래스, 찾지 못하면 null
     */
    public Class<?> resolveConcreteType(Class<?> baseType, String typeName) {
        TypeInfo typeInfo = getTypeInfo(baseType);
        if (typeInfo == null) {
            return null;
        }
        
        Class<?> concreteType = typeInfo.getConcreteType(typeName);
        if (concreteType != null) {
            return concreteType;
        }
        
        // 기본 구현체 확인
        if (typeInfo.hasDefaultImpl()) {
            return typeInfo.getDefaultImpl();
        }
        
        return null;
    }
    
    /**
     * 클래스가 다형성 타입인지 확인합니다.
     * @param clazz 확인할 클래스
     * @return 다형성 타입이면 true
     */
    public boolean isPolymorphicType(Class<?> clazz) {
        return analyzer.isPolymorphicType(clazz);
    }
    
    /**
     * 캐시를 초기화합니다.
     */
    public void clearCache() {
        typeInfoMap.clear();
    }
    
    /**
     * 캐시된 타입 정보의 개수를 반환합니다.
     */
    public int getCacheSize() {
        return typeInfoMap.size();
    }
}
