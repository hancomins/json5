package com.hancomins.json5.serializer.constructor;

import com.hancomins.json5.serializer.MissingValueStrategy;

/**
 * 생성자 파라미터의 정보를 담는 클래스입니다.
 */
public class ParameterInfo {
    private final String jsonPath;
    private final Class<?> parameterType;
    private final MissingValueStrategy missingStrategy;
    private final boolean required;
    private final int parameterIndex;
    
    /**
     * ParameterInfo 생성자
     * 
     * @param jsonPath JSON 경로
     * @param parameterType 파라미터 타입
     * @param missingStrategy 누락 값 처리 전략
     * @param required 필수 여부
     * @param parameterIndex 파라미터 인덱스
     */
    public ParameterInfo(String jsonPath, Class<?> parameterType, 
                        MissingValueStrategy missingStrategy, boolean required, 
                        int parameterIndex) {
        this.jsonPath = jsonPath;
        this.parameterType = parameterType;
        this.missingStrategy = missingStrategy;
        this.required = required;
        this.parameterIndex = parameterIndex;
    }
    
    /**
     * JSON 경로를 반환합니다.
     * 
     * @return JSON 경로
     */
    public String getJsonPath() {
        return jsonPath;
    }
    
    /**
     * 파라미터 타입을 반환합니다.
     * 
     * @return 파라미터 타입
     */
    public Class<?> getParameterType() {
        return parameterType;
    }
    
    /**
     * 누락 값 처리 전략을 반환합니다.
     * 
     * @return 누락 값 처리 전략
     */
    public MissingValueStrategy getMissingStrategy() {
        return missingStrategy;
    }
    
    /**
     * 필수 여부를 반환합니다.
     * 
     * @return 필수 여부
     */
    public boolean isRequired() {
        return required;
    }
    
    /**
     * 파라미터 인덱스를 반환합니다.
     * 
     * @return 파라미터 인덱스
     */
    public int getParameterIndex() {
        return parameterIndex;
    }
    
    @Override
    public String toString() {
        return "ParameterInfo{" +
                "jsonPath='" + jsonPath + '\'' +
                ", parameterType=" + parameterType.getSimpleName() +
                ", missingStrategy=" + missingStrategy +
                ", required=" + required +
                ", parameterIndex=" + parameterIndex +
                '}';
    }
}
