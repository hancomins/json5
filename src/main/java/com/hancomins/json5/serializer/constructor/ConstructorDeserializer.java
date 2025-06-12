package com.hancomins.json5.serializer.constructor;

import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.serializer.JSON5SerializerException;
import com.hancomins.json5.serializer.path.JSON5PathExtractor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 생성자 기반 역직렬화를 수행합니다.
 */
public class ConstructorDeserializer {
    
    private final ConstructorAnalyzer constructorAnalyzer;
    private final ParameterValueResolver parameterValueResolver;
    
    /**
     * ConstructorDeserializer 생성자
     */
    public ConstructorDeserializer() {
        this.constructorAnalyzer = new ConstructorAnalyzer();
        this.parameterValueResolver = new ParameterValueResolver();
    }
    
    /**
     * JSON5Object를 사용하여 지정된 클래스의 인스턴스를 생성합니다.
     * 
     * @param json5Object JSON 객체
     * @param clazz 대상 클래스
     * @param <T> 반환 타입
     * @return 생성된 인스턴스
     * @throws JSON5SerializerException 역직렬화 실패 시
     */
    public <T> T deserialize(JSON5Object json5Object, Class<T> clazz) {
        if (json5Object == null) {
            throw new JSON5SerializerException("JSON5Object cannot be null");
        }
        if (clazz == null) {
            throw new JSON5SerializerException("Target class cannot be null");
        }
        
        // 생성자 분석
        List<ConstructorInfo> constructors = constructorAnalyzer.analyzeConstructors(clazz);
        if (constructors.isEmpty()) {
            throw new JSON5SerializerException(
                "No @JSON5Creator constructor found for class: " + clazz.getName()
            );
        }
        
        // 가장 적합한 생성자 선택
        ConstructorInfo bestConstructor = constructorAnalyzer.selectBestConstructor(constructors);
        if (bestConstructor == null) {
            throw new JSON5SerializerException(
                "No suitable constructor found for class: " + clazz.getName()
            );
        }
        
        return deserialize(json5Object, bestConstructor);
    }
    
    /**
     * 특정 생성자 정보를 사용하여 역직렬화를 수행합니다.
     * 
     * @param json5Object JSON 객체
     * @param constructorInfo 사용할 생성자 정보
     * @param <T> 반환 타입
     * @return 생성된 인스턴스
     * @throws JSON5SerializerException 역직렬화 실패 시
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(JSON5Object json5Object, ConstructorInfo constructorInfo) {
        if (json5Object == null || constructorInfo == null) {
            throw new JSON5SerializerException("JSON5Object and ConstructorInfo cannot be null");
        }
        
        try {
            // 파라미터 값들 해석
            Object[] parameterValues = resolveParameterValues(json5Object, constructorInfo.getParameters());
            
            // 생성자 호출
            Constructor<?> constructor = constructorInfo.getConstructor();
            constructor.setAccessible(true);
            
            Object instance = constructor.newInstance(parameterValues);
            return (T) instance;
            
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof JSON5SerializerException) {
                throw (JSON5SerializerException) cause;
            }
            throw new JSON5SerializerException(
                "Failed to invoke constructor: " + cause.getMessage(), cause
            );
        } catch (InstantiationException | IllegalAccessException e) {
            throw new JSON5SerializerException(
                "Failed to create instance: " + e.getMessage(), e
            );
        } catch (Exception e) {
            throw new JSON5SerializerException(
                "Unexpected error during deserialization: " + e.getMessage(), e
            );
        }
    }
    
    /**
     * 클래스에 @JSON5Creator 생성자가 있는지 확인합니다.
     * 
     * @param clazz 확인할 클래스
     * @return @JSON5Creator 생성자 존재 여부
     */
    public boolean canDeserialize(Class<?> clazz) {
        return constructorAnalyzer.hasCreatorConstructor(clazz);
    }
    
    /**
     * 파라미터 값들을 해석합니다.
     * 
     * @param json5Object JSON 객체
     * @param parameters 파라미터 정보 리스트
     * @return 해석된 파라미터 값 배열
     * @throws JSON5SerializerException 값 해석 실패 시
     */
    private Object[] resolveParameterValues(JSON5Object json5Object, List<ParameterInfo> parameters) {
        Object[] values = new Object[parameters.size()];
        
        for (int i = 0; i < parameters.size(); i++) {
            ParameterInfo parameterInfo = parameters.get(i);
            Object value = resolveParameterValue(json5Object, parameterInfo);
            values[i] = value;
        }
        
        return values;
    }
    
    /**
     * 개별 파라미터 값을 해석합니다.
     * 
     * @param json5Object JSON 객체
     * @param parameterInfo 파라미터 정보
     * @return 해석된 값
     * @throws JSON5SerializerException 값 해석 실패 시
     */
    private Object resolveParameterValue(JSON5Object json5Object, ParameterInfo parameterInfo) {
        try {
            // JSON에서 경로에 따라 값 추출
            Object extractedValue = JSON5PathExtractor.extractValue(json5Object, parameterInfo.getJsonPath());
            
            // JSON5Element를 실제 값으로 변환
            Object actualValue = extractActualValue(extractedValue);
            
            // 파라미터 타입에 맞게 값 변환
            return parameterValueResolver.resolveValue(actualValue, parameterInfo.getParameterType(), parameterInfo);
            
        } catch (JSON5SerializerException e) {
            throw e;
        } catch (Exception e) {
            throw new JSON5SerializerException(
                "Failed to resolve parameter '" + parameterInfo.getJsonPath() + "': " + e.getMessage(), e
            );
        }
    }
    
    /**
     * JSON5Element에서 실제 값을 추출합니다.
     * 
     * @param extractedValue 추출된 JSON5Element 또는 마커 객체
     * @return 실제 값
     */
    private Object extractActualValue(Object extractedValue) {
        // 누락된 값 마커인 경우 그대로 반환
        if (JSON5PathExtractor.isMissingValue(extractedValue)) {
            return extractedValue;
        }
        
        // JSON5Element인 경우 실제 값 추출
        if (extractedValue instanceof JSON5Element) {
            JSON5Element element = (JSON5Element) extractedValue;
            
            // JSON5Object나 JSON5Array의 경우는 그대로 반환 (추후 복합 타입 처리용)
            if (element instanceof JSON5Object || element instanceof JSON5Array) {
                return element;
            }
            
            // 원시 값인 경우 - 이 경우는 현재 JSON5 라이브러리 구조에서는 거의 없음
            // JSON5Object.get()이 이미 원시 값을 반환하기 때문
            return extractedValue;
        }
        
        // 이미 기본 타입인 경우 그대로 반환
        return extractedValue;
    }
}
