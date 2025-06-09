package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Exception;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * JSON5 Serializer의 개선된 기본 예외 클래스입니다.
 * 
 * <p>기존 JSON5SerializerException을 확장하여 에러 코드와 컨텍스트 정보를 
 * 추가로 제공합니다. 이를 통해 더 정확한 오류 진단과 처리가 가능합니다.</p>
 * 
 * <h3>주요 개선사항:</h3>
 * <ul>
 *   <li>에러 코드를 통한 구체적인 오류 분류</li>
 *   <li>컨텍스트 정보를 통한 상세한 오류 상황 전달</li>
 *   <li>체이닝된 예외 정보의 체계적 관리</li>
 * </ul>
 * 
 * <h3>사용 예제:</h3>
 * <pre>{@code
 * throw new JSON5SerializerException("INVALID_TYPE", "타입 변환 실패")
 *     .addContext("sourceType", sourceType)
 *     .addContext("targetType", targetType)
 *     .addContext("value", value);
 * }</pre>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class JSON5SerializerException extends JSON5Exception {
    
    /** 에러 코드 */
    private final String errorCode;
    
    /** 컨텍스트 정보 맵 */
    private final Map<String, Object> context;
    
    /**
     * 메시지만으로 예외를 생성합니다.
     * 
     * @param message 오류 메시지
     */
    public JSON5SerializerException(String message) {
        super(message);
        this.errorCode = "GENERAL_ERROR";
        this.context = new HashMap<>();
    }
    
    /**
     * 에러 코드와 메시지로 예외를 생성합니다.
     * 
     * @param errorCode 에러 코드
     * @param message 오류 메시지
     */
    public JSON5SerializerException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : "GENERAL_ERROR";
        this.context = new HashMap<>();
    }

    /**
     * 메시지와 원인 예외로 예외를 생성합니다.
     * 
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public JSON5SerializerException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GENERAL_ERROR";
        this.context = new HashMap<>();
    }
    
    /**
     * 에러 코드, 메시지, 원인 예외로 예외를 생성합니다.
     * 
     * @param errorCode 에러 코드
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public JSON5SerializerException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : "GENERAL_ERROR";
        this.context = new HashMap<>();
    }

    /**
     * 원인 예외만으로 예외를 생성합니다.
     * 
     * @param cause 원인 예외
     */
    public JSON5SerializerException(Throwable cause) {
        super(cause);
        this.errorCode = "GENERAL_ERROR";
        this.context = new HashMap<>();
    }
    
    /**
     * 에러 코드를 반환합니다.
     * 
     * @return 에러 코드
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 컨텍스트 정보를 추가합니다.
     * 
     * @param key 컨텍스트 키
     * @param value 컨텍스트 값
     * @return 메소드 체이닝을 위한 현재 예외 인스턴스
     */
    public JSON5SerializerException addContext(String key, Object value) {
        if (key != null) {
            this.context.put(key, value);
        }
        return this;
    }
    
    /**
     * 여러 컨텍스트 정보를 한번에 추가합니다.
     * 
     * @param contextMap 추가할 컨텍스트 맵
     * @return 메소드 체이닝을 위한 현재 예외 인스턴스
     */
    public JSON5SerializerException addContext(Map<String, Object> contextMap) {
        if (contextMap != null) {
            this.context.putAll(contextMap);
        }
        return this;
    }
    
    /**
     * 특정 키의 컨텍스트 값을 반환합니다.
     * 
     * @param key 컨텍스트 키
     * @return 컨텍스트 값, 없으면 null
     */
    public Object getContext(String key) {
        return context.get(key);
    }
    
    /**
     * 모든 컨텍스트 정보를 읽기 전용 맵으로 반환합니다.
     * 
     * @return 읽기 전용 컨텍스트 맵
     */
    public Map<String, Object> getAllContext() {
        return Collections.unmodifiableMap(context);
    }
    
    /**
     * 컨텍스트 정보가 있는지 확인합니다.
     * 
     * @return 컨텍스트 정보가 있으면 true, 없으면 false
     */
    public boolean hasContext() {
        return !context.isEmpty();
    }
    
    /**
     * 특정 키의 컨텍스트가 있는지 확인합니다.
     * 
     * @param key 확인할 컨텍스트 키
     * @return 해당 키의 컨텍스트가 있으면 true, 없으면 false
     */
    public boolean hasContext(String key) {
        return context.containsKey(key);
    }
    
    /**
     * 상세한 오류 정보를 포함한 문자열을 반환합니다.
     * 
     * @return 상세 오류 정보 문자열
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("ErrorCode: ").append(errorCode);
        sb.append(", Message: ").append(getMessage());
        
        if (hasContext()) {
            sb.append(", Context: {");
            boolean first = true;
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            sb.append("}");
        }
        
        if (getCause() != null) {
            sb.append(", Cause: ").append(getCause().getMessage());
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getClass().getName() + ": " + getDetailedMessage();
    }
}
