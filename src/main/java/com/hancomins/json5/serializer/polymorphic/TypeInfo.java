package com.hancomins.json5.serializer.polymorphic;

import com.hancomins.json5.serializer.TypeInclusion;
import com.hancomins.json5.serializer.MissingTypeStrategy;

import java.util.Map;

/**
 * 다형성 타입의 정보를 담는 클래스입니다.
 */
public class TypeInfo {
    private final String typeProperty;
    private final TypeInclusion inclusion;
    private final Class<?> defaultImpl;
    private final MissingTypeStrategy missingStrategy;
    private final Map<String, Class<?>> subTypes;
    
    public TypeInfo(String typeProperty, TypeInclusion inclusion, Class<?> defaultImpl, 
                   MissingTypeStrategy missingStrategy, Map<String, Class<?>> subTypes) {
        this.typeProperty = typeProperty;
        this.inclusion = inclusion;
        this.defaultImpl = defaultImpl;
        this.missingStrategy = missingStrategy;
        this.subTypes = subTypes;
    }
    
    /**
     * 타입 정보를 포함하는 JSON 속성 경로를 반환합니다.
     */
    public String getTypeProperty() {
        return typeProperty;
    }
    
    /**
     * 타입 정보 포함 방식을 반환합니다.
     */
    public TypeInclusion getInclusion() {
        return inclusion;
    }
    
    /**
     * 기본 구현체 클래스를 반환합니다.
     */
    public Class<?> getDefaultImpl() {
        return defaultImpl;
    }
    
    /**
     * 타입 정보가 없을 때의 처리 전략을 반환합니다.
     */
    public MissingTypeStrategy getMissingStrategy() {
        return missingStrategy;
    }
    
    /**
     * 서브타입 매핑을 반환합니다.
     */
    public Map<String, Class<?>> getSubTypes() {
        return subTypes;
    }
    
    /**
     * 기본 구현체가 설정되어 있는지 확인합니다.
     */
    public boolean hasDefaultImpl() {
        return defaultImpl != null && defaultImpl != Void.class;
    }
    
    /**
     * 타입 이름으로 구체적인 클래스를 찾습니다.
     * @param typeName 타입 이름
     * @return 매칭되는 클래스, 없으면 null
     */
    public Class<?> getConcreteType(String typeName) {
        return subTypes.get(typeName);
    }
    
    /**
     * 지원하는 타입 이름들을 반환합니다.
     */
    public java.util.Set<String> getSupportedTypeNames() {
        return subTypes.keySet();
    }
    
    @Override
    public String toString() {
        return String.format("TypeInfo{property='%s', inclusion=%s, defaultImpl=%s, subTypes=%s}", 
                           typeProperty, inclusion, 
                           hasDefaultImpl() ? defaultImpl.getSimpleName() : "none",
                           subTypes.keySet());
    }
}
