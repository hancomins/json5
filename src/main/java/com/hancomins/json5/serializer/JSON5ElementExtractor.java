package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;

/**
 * JSON5Element에서 특정 타입의 값을 추출하는 유틸리티 클래스입니다.
 * 
 * <p>JSON5Array 또는 JSON5Object에서 키나 인덱스를 사용하여 
 * 지정된 타입의 값을 안전하게 추출하는 기능을 제공합니다.
 * null 값 처리와 타입별 적절한 메소드 호출을 담당합니다.</p>
 * 
 * <h3>사용 예제:</h3>
 * <pre>{@code
 * JSON5Object json5Object = new JSON5Object();
 * json5Object.put("name", "John");
 * 
 * String name = (String) JSON5ElementExtractor.getFrom(json5Object, "name", Types.String);
 * }</pre>
 * 
 * <h3>지원하는 타입:</h3>
 * <ul>
 *   <li>Boolean - boolean 값 추출</li>
 *   <li>Byte - byte 값 추출</li>
 *   <li>Character - char 값 추출 (기본값: '\0')</li>
 *   <li>Short - short 값 추출</li>
 *   <li>Integer - int 값 추출</li>
 *   <li>Float - float 값 추출</li>
 *   <li>Double - double 값 추출</li>
 *   <li>String - String 값 추출</li>
 *   <li>ByteArray - byte[] 값 추출</li>
 *   <li>기타 - 일반 Object로 추출</li>
 * </ul>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public final class JSON5ElementExtractor {
    
    /**
     * JSON5Element에서 지정된 키/인덱스와 타입에 해당하는 값을 추출합니다.
     * 
     * <p>JSON5Array의 경우 key는 Integer 타입이어야 하고,
     * JSON5Object의 경우 key는 String 타입이어야 합니다.
     * null 값인 경우 null을 반환합니다.</p>
     * 
     * @param json5 값을 추출할 JSON5Element (JSON5Array 또는 JSON5Object)
     * @param key 추출할 값의 키 (JSON5Object의 경우 String, JSON5Array의 경우 Integer)
     * @param valueType 추출할 값의 타입
     * @return 추출된 값, null인 경우 null
     * @throws IllegalArgumentException json5가 null이거나 지원되지 않는 타입인 경우
     * @throws ClassCastException key 타입이 json5 타입과 맞지 않는 경우
     */
    public static Object getFrom(JSON5Element json5, Object key, Types valueType) {
        if (json5 == null) {
            throw new IllegalArgumentException("json5 cannot be null");
        }
        if (valueType == null) {
            throw new IllegalArgumentException("valueType cannot be null");
        }
        
        boolean isArrayType = json5 instanceof JSON5Array;
        
        // null 값 체크
        if (isArrayType) {
            if (!(key instanceof Integer)) {
                throw new ClassCastException("Key must be Integer for JSON5Array");
            }
            int index = (Integer) key;
            if (((JSON5Array) json5).isNull(index)) {
                return null;
            }
        } else if (json5 instanceof JSON5Object) {
            if (!(key instanceof String)) {
                throw new ClassCastException("Key must be String for JSON5Object");
            }
            String stringKey = (String) key;
            if (((JSON5Object) json5).isNull(stringKey)) {
                return null;
            }
        } else {
            throw new IllegalArgumentException("Unsupported JSON5Element type: " + json5.getClass());
        }
        
        return extractValueByType(json5, key, valueType, isArrayType);
    }
    
    /**
     * 타입별로 적절한 메소드를 호출하여 값을 추출합니다.
     * 
     * @param json5 JSON5Element
     * @param key 키 또는 인덱스
     * @param valueType 값 타입
     * @param isArrayType JSON5Array 여부
     * @return 추출된 값
     */
    private static Object extractValueByType(JSON5Element json5, Object key, Types valueType, boolean isArrayType) {
        switch (valueType) {
            case Boolean:
                return isArrayType 
                    ? ((JSON5Array) json5).getBoolean((Integer) key) 
                    : ((JSON5Object) json5).getBoolean((String) key);
                    
            case Byte:
                return isArrayType 
                    ? ((JSON5Array) json5).getByte((Integer) key) 
                    : ((JSON5Object) json5).getByte((String) key);
                    
            case Character:
                return isArrayType 
                    ? ((JSON5Array) json5).getChar((Integer) key, SerializerConstants.DEFAULT_CHAR_VALUE) 
                    : ((JSON5Object) json5).getChar((String) key, SerializerConstants.DEFAULT_CHAR_VALUE);
                    
            case Short:
                return isArrayType 
                    ? ((JSON5Array) json5).getShort((Integer) key) 
                    : ((JSON5Object) json5).getShort((String) key);
                    
            case Integer:
                return isArrayType 
                    ? ((JSON5Array) json5).getInt((Integer) key) 
                    : ((JSON5Object) json5).getInt((String) key);
                    
            case Float:
                return isArrayType 
                    ? ((JSON5Array) json5).getFloat((Integer) key) 
                    : ((JSON5Object) json5).getFloat((String) key);
                    
            case Double:
                return isArrayType 
                    ? ((JSON5Array) json5).getDouble((Integer) key) 
                    : ((JSON5Object) json5).getDouble((String) key);
                    
            case String:
                return isArrayType 
                    ? ((JSON5Array) json5).getString((Integer) key) 
                    : ((JSON5Object) json5).getString((String) key);
                    
            case ByteArray:
                return isArrayType 
                    ? ((JSON5Array) json5).getByteArray((Integer) key) 
                    : ((JSON5Object) json5).getByteArray((String) key);
                    
            default:
                return isArrayType 
                    ? ((JSON5Array) json5).get((Integer) key) 
                    : ((JSON5Object) json5).opt((String) key);
        }
    }
    
    /**
     * JSON5Element에서 안전하게 값을 추출합니다. 예외가 발생하면 기본값을 반환합니다.
     * 
     * @param json5 값을 추출할 JSON5Element
     * @param key 추출할 값의 키
     * @param valueType 추출할 값의 타입
     * @param defaultValue 예외 발생 시 반환할 기본값
     * @return 추출된 값 또는 기본값
     */
    public static Object getFromSafely(JSON5Element json5, Object key, Types valueType, Object defaultValue) {
        try {
            return getFrom(json5, key, valueType);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * JSON5Element가 JSON5Array인지 확인합니다.
     * 
     * @param json5 확인할 JSON5Element
     * @return JSON5Array이면 true, 그렇지 않으면 false
     */
    public static boolean isArray(JSON5Element json5) {
        return json5 instanceof JSON5Array;
    }
    
    /**
     * JSON5Element가 JSON5Object인지 확인합니다.
     * 
     * @param json5 확인할 JSON5Element
     * @return JSON5Object이면 true, 그렇지 않으면 false
     */
    public static boolean isObject(JSON5Element json5) {
        return json5 instanceof JSON5Object;
    }
    
    /**
     * 인스턴스화를 방지하는 private 생성자입니다.
     */
    private JSON5ElementExtractor() {
        throw new AssertionError("JSON5ElementExtractor cannot be instantiated");
    }
}
