package com.hancomins.json5.serializer.constructor;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * @JSON5Creator가 붙은 생성자의 정보를 담는 클래스입니다.
 */
public class ConstructorInfo {
    private final Constructor<?> constructor;
    private final int priority;
    private final List<ParameterInfo> parameters;
    
    /**
     * ConstructorInfo 생성자
     * 
     * @param constructor 생성자 객체
     * @param priority 우선순위
     * @param parameters 파라미터 정보 리스트
     */
    public ConstructorInfo(Constructor<?> constructor, int priority, 
                          List<ParameterInfo> parameters) {
        this.constructor = constructor;
        this.priority = priority;
        this.parameters = parameters;
    }
    
    /**
     * 생성자 객체를 반환합니다.
     * 
     * @return 생성자 객체
     */
    public Constructor<?> getConstructor() {
        return constructor;
    }
    
    /**
     * 우선순위를 반환합니다.
     * 
     * @return 우선순위
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * 파라미터 정보 리스트를 반환합니다.
     * 
     * @return 파라미터 정보 리스트
     */
    public List<ParameterInfo> getParameters() {
        return parameters;
    }
    
    /**
     * 파라미터 개수를 반환합니다.
     * 
     * @return 파라미터 개수
     */
    public int getParameterCount() {
        return parameters.size();
    }
    
    @Override
    public String toString() {
        return "ConstructorInfo{" +
                "constructor=" + constructor.getName() +
                ", priority=" + priority +
                ", parameterCount=" + parameters.size() +
                '}';
    }
}
