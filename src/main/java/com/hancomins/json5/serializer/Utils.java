package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Element;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * JSON5 Serializer의 유틸리티 메소드들을 제공하는 클래스입니다.
 * 
 * <p><strong>주의:</strong> 이 클래스의 모든 메소드들은 deprecated되었습니다.
 * 새로운 코드에서는 다음의 전용 클래스들을 사용하시기 바랍니다:</p>
 * <ul>
 *   <li>{@link TypeConverter} - 타입 변환 관련 기능</li>
 *   <li>{@link PrimitiveTypeConverter} - Primitive 타입 변환</li>
 *   <li>{@link JSON5ElementExtractor} - JSON5Element에서 값 추출</li>
 * </ul>
 * 
 * <p>이 클래스는 하위 호환성을 위해 유지되며, 향후 버전에서 제거될 예정입니다.</p>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 1.0
 * @deprecated 2.0에서 deprecated됨. 대신 {@link TypeConverter}, {@link PrimitiveTypeConverter}, {@link JSON5ElementExtractor}를 사용하세요.
 */
@Deprecated
public class Utils {

    /**
     * Primitive 타입을 Boxed 타입으로 변환합니다.
     * 
     * @param primitiveType 변환할 primitive 타입
     * @return 해당하는 Boxed 타입
     * @deprecated 2.0에서 deprecated됨. 대신 {@link PrimitiveTypeConverter#primitiveTypeToBoxedType(Class)}를 사용하세요.
     */
    @Deprecated
    static Class<?> primitiveTypeToBoxedType(Class<?> primitiveType) {
        return PrimitiveTypeConverter.primitiveTypeToBoxedType(primitiveType);
    }

    /**
     * 컬렉션 값을 지정된 타입으로 변환합니다.
     * 
     * @param origin 변환할 원본 컬렉션
     * @param resultCollectionItemsList 결과 컬렉션의 타입 정보
     * @param returnType 최종 요소의 타입
     * @return 변환된 컬렉션
     * @throws InvocationTargetException 컬렉션 생성자 호출 시 예외
     * @throws InstantiationException 컬렉션 인스턴스 생성 시 예외
     * @throws IllegalAccessException 컬렉션 생성자 접근 시 예외
     * @deprecated 2.0에서 deprecated됨. 대신 {@link TypeConverter#convertCollectionValue(Object, List, Types)}를 사용하세요.
     */
    @Deprecated
    static Object convertCollectionValue(Object origin, List<CollectionItems> resultCollectionItemsList, Types returnType)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return TypeConverter.convertCollectionValue(origin, resultCollectionItemsList, returnType);
    }

    /**
     * 값을 지정된 타입으로 변환합니다.
     * 
     * @param origin 변환할 원본 값
     * @param returnType 변환할 대상 타입
     * @return 변환된 값, 변환 실패 시 null
     * @deprecated 2.0에서 deprecated됨. 대신 {@link TypeConverter#convertValue(Object, Types)}를 사용하세요.
     */
    @Deprecated
    static Object convertValue(Object origin, Types returnType) {
        return TypeConverter.convertValue(origin, returnType);
    }

    /**
     * String 값을 지정된 타입으로 변환합니다.
     * 
     * @param origin 변환할 문자열
     * @param returnType 변환할 대상 타입
     * @return 변환된 값, 변환 실패 시 null
     * @deprecated 2.0에서 deprecated됨. 대신 {@link TypeConverter#convertValueFromString(String, Types)}를 사용하세요.
     */
    @Deprecated
    static Object convertValueFromString(String origin, Types returnType) {
        return TypeConverter.convertValueFromString(origin, returnType);
    }

    /**
     * Number 값을 지정된 타입으로 변환합니다.
     * 
     * @param origin 변환할 Number 값
     * @param returnType 변환할 대상 타입
     * @return 변환된 값, 변환 실패 시 null
     * @deprecated 2.0에서 deprecated됨. 대신 {@link TypeConverter#convertValueFromNumber(Number, Types)}를 사용하세요.
     */
    @Deprecated
    static Object convertValueFromNumber(Number origin, Types returnType) {
        return TypeConverter.convertValueFromNumber(origin, returnType);
    }

    /**
     * JSON5Element에서 지정된 키/인덱스와 타입에 해당하는 값을 추출합니다.
     * 
     * @param json5 값을 추출할 JSON5Element
     * @param key 추출할 값의 키
     * @param valueType 추출할 값의 타입
     * @return 추출된 값, null인 경우 null
     * @deprecated 2.0에서 deprecated됨. 대신 {@link JSON5ElementExtractor#getFrom(JSON5Element, Object, Types)}를 사용하세요.
     */
    @Deprecated
    static Object getFrom(JSON5Element json5, Object key, Types valueType) {
        return JSON5ElementExtractor.getFrom(json5, key, valueType);
    }
}
