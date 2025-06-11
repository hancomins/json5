package com.hancomins.json5.serializer.polymorphic;

import com.hancomins.json5.serializer.JSON5TypeInfo;
import com.hancomins.json5.serializer.JSON5SubType;
import com.hancomins.json5.serializer.JSON5SubTypes;
import com.hancomins.json5.serializer.TypeInclusion;
import com.hancomins.json5.serializer.MissingTypeStrategy;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 클래스의 타입 정보 어노테이션을 분석합니다.
 * @JSON5TypeInfo와 @JSON5SubType 어노테이션을 읽어 다형성 정보를 추출합니다.
 */
public class TypeInfoAnalyzer {
    
    /**
     * 클래스의 타입 정보를 분석합니다.
     * @param clazz 분석할 클래스
     * @return 타입 정보 객체, 다형성 타입이 아니면 null
     */
    public TypeInfo analyzeTypeInfo(Class<?> clazz) {
        JSON5TypeInfo typeInfo = clazz.getAnnotation(JSON5TypeInfo.class);
        if (typeInfo == null) {
            return null;
        }
        
        Map<String, Class<?>> subTypes = new HashMap<>();
        
        // @JSON5SubType 어노테이션들 수집
        JSON5SubType[] subTypeAnnotations = clazz.getAnnotationsByType(JSON5SubType.class);
        for (JSON5SubType subType : subTypeAnnotations) {
            subTypes.put(subType.name(), subType.value());
        }
        
        return new TypeInfo(
            typeInfo.property(),
            typeInfo.include(),
            typeInfo.defaultImpl(),
            typeInfo.onMissingType(),
            subTypes
        );
    }
    
    /**
     * 필드의 타입 정보를 분석합니다.
     * @param field 분석할 필드
     * @return 타입 정보 객체, 다형성 타입이 아니면 null
     */
    public TypeInfo analyzeFieldTypeInfo(Field field) {
        JSON5TypeInfo typeInfo = field.getAnnotation(JSON5TypeInfo.class);
        if (typeInfo == null) {
            // 필드에 어노테이션이 없으면 필드 타입의 어노테이션 확인
            return analyzeTypeInfo(field.getType());
        }
        
        Map<String, Class<?>> subTypes = new HashMap<>();
        
        // 필드 타입에서 @JSON5SubType 어노테이션들 수집
        Class<?> fieldType = field.getType();
        JSON5SubType[] subTypeAnnotations = fieldType.getAnnotationsByType(JSON5SubType.class);
        for (JSON5SubType subType : subTypeAnnotations) {
            subTypes.put(subType.name(), subType.value());
        }
        
        return new TypeInfo(
            typeInfo.property(),
            typeInfo.include(),
            typeInfo.defaultImpl(),
            typeInfo.onMissingType(),
            subTypes
        );
    }
    
    /**
     * 클래스가 다형성 타입인지 확인합니다.
     * @param clazz 확인할 클래스
     * @return 다형성 타입이면 true
     */
    public boolean isPolymorphicType(Class<?> clazz) {
        return clazz.isAnnotationPresent(JSON5TypeInfo.class);
    }
    
    /**
     * 필드가 다형성 타입인지 확인합니다.
     * @param field 확인할 필드
     * @return 다형성 타입이면 true
     */
    public boolean isPolymorphicField(Field field) {
        // 필드에 직접 어노테이션이 있거나, 필드 타입에 어노테이션이 있으면 다형성
        return field.isAnnotationPresent(JSON5TypeInfo.class) || 
               isPolymorphicType(field.getType());
    }
}
