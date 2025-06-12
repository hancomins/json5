package com.hancomins.json5.serializer.provider;

import com.hancomins.json5.serializer.JSON5ValueExtractor;
import com.hancomins.json5.serializer.NullHandling;

import java.lang.reflect.Method;

/**
 * @JSON5ValueExtractor 메서드 정보를 담는 클래스
 */
public class ExtractorInfo {
    private final Method method;
    private final Class<?> returnType;
    private final NullHandling nullHandling;
    
    public ExtractorInfo(Method method) {
        this.method = method;
        this.returnType = method.getReturnType();
        
        JSON5ValueExtractor annotation = method.getAnnotation(JSON5ValueExtractor.class);
        this.nullHandling = annotation.onNull();
    }
    
    public Method getMethod() {
        return method;
    }
    
    public Class<?> getReturnType() {
        return returnType;
    }
    
    public NullHandling getNullHandling() {
        return nullHandling;
    }
    
    @Override
    public String toString() {
        return "ExtractorInfo{" +
                "method=" + method.getName() +
                ", returnType=" + returnType.getSimpleName() +
                ", nullHandling=" + nullHandling +
                '}';
    }
}
