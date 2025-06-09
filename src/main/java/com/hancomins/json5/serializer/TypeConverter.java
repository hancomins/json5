package com.hancomins.json5.serializer;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 다양한 타입 간의 변환을 담당하는 유틸리티 클래스입니다.
 * 
 * <p>이 클래스는 Object, String, Number 타입의 값을 지정된 Types enum에 따라
 * 적절한 타입으로 변환하는 기능을 제공합니다. 또한 복잡한 컬렉션 타입의 
 * 변환도 지원합니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>String에서 다양한 타입으로의 변환</li>
 *   <li>Number에서 다양한 숫자 타입으로의 변환</li>
 *   <li>중첩된 컬렉션의 타입 변환</li>
 *   <li>안전한 타입 변환 (예외 처리 포함)</li>
 * </ul>
 * 
 * <h3>사용 예제:</h3>
 * <pre>{@code
 * // String을 Integer로 변환
 * Object result = TypeConverter.convertValueFromString("123", Types.Integer);
 * 
 * // Number를 Double로 변환
 * Object result = TypeConverter.convertValueFromNumber(123, Types.Double);
 * 
 * // 일반적인 타입 변환
 * Object result = TypeConverter.convertValue("123", Types.Integer);
 * }</pre>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public final class TypeConverter {
    
    /**
     * 주어진 값을 지정된 타입으로 변환합니다.
     * 
     * <p>입력 값의 타입에 따라 적절한 변환 메소드를 호출합니다.
     * String 타입이면 convertValueFromString을, Number 타입이면 
     * convertValueFromNumber를 호출합니다.</p>
     * 
     * @param origin 변환할 원본 값
     * @param returnType 변환할 대상 타입
     * @return 변환된 값, 변환 실패 시 null
     */
    public static Object convertValue(Object origin, Types returnType) {
        if (origin == null) {
            return null;
        }
        
        try {
            if (origin instanceof String) {
                return convertValueFromString((String) origin, returnType);
            } else if (origin instanceof Number) {
                return convertValueFromNumber((Number) origin, returnType);
            } else {
                // 다른 타입의 경우 toString()으로 변환 후 처리
                return convertValueFromString(origin.toString(), returnType);
            }
        } catch (NumberFormatException e) {
            // NumberFormatException은 무시하고 null 반환 (기존 동작 유지)
            return null;
        } catch (Exception e) {
            // 기타 예외 발생 시 null 반환
            return null;
        }
    }
    
    /**
     * String 값을 지정된 타입으로 변환합니다.
     * 
     * <p>문자열을 파싱하여 해당하는 타입의 값으로 변환합니다.
     * 각 타입별로 적절한 valueOf 메소드나 생성자를 사용합니다.</p>
     * 
     * @param origin 변환할 문자열 (null 허용)
     * @param returnType 변환할 대상 타입
     * @return 변환된 값, origin이 null이거나 변환 실패 시 null
     * @throws NumberFormatException 숫자 변환 시 형식이 잘못된 경우
     */
    public static Object convertValueFromString(String origin, Types returnType) {
        if (origin == null) {
            return null;
        }
        
        switch (returnType) {
            case String:
                return origin;
            case Byte:
                return Byte.valueOf(origin);
            case Short:
                return Short.valueOf(origin);
            case Integer:
                return Integer.valueOf(origin);
            case Long:
                return Long.valueOf(origin);
            case Float:
                return Float.valueOf(origin);
            case Double:
                return Double.valueOf(origin);
            case Character:
                return origin.length() > 0 ? origin.charAt(0) : SerializerConstants.DEFAULT_CHAR_VALUE;
            case Boolean:
                return Boolean.valueOf(origin);
            case BigDecimal:
                return new BigDecimal(origin);
            case BigInteger:
                return new BigInteger(origin);
            default:
                return null;
        }
    }
    
    /**
     * Number 값을 지정된 타입으로 변환합니다.
     * 
     * <p>이미 올바른 타입인 경우 그대로 반환하고, 그렇지 않은 경우
     * 적절한 변환 메소드를 사용하여 타입을 변경합니다.</p>
     * 
     * @param origin 변환할 Number 값 (null 허용)
     * @param returnType 변환할 대상 타입
     * @return 변환된 값, origin이 null이거나 변환 실패 시 null
     */
    public static Object convertValueFromNumber(Number origin, Types returnType) {
        if (origin == null) {
            return null;
        }
        
        // 이미 올바른 타입인 경우 그대로 반환
        if (isAlreadyCorrectType(origin, returnType)) {
            return origin;
        }
        
        switch (returnType) {
            case Byte:
                return origin.byteValue();
            case Short:
                return origin.shortValue();
            case Integer:
                return origin.intValue();
            case Long:
                return origin.longValue();
            case Float:
                return origin.floatValue();
            case Double:
                return origin.doubleValue();
            case Character:
                return (char) origin.intValue();
            case Boolean:
                return origin.intValue() != 0;
            case BigDecimal:
                return new BigDecimal(origin.toString());
            case BigInteger:
                return new BigInteger(origin.toString());
            case String:
                return origin.toString();
            case ByteArray:
                return new byte[]{origin.byteValue()};
            default:
                return null;
        }
    }
    
    /**
     * 복잡한 중첩 컬렉션을 지정된 타입으로 변환합니다.
     * 
     * <p>여러 단계로 중첩된 컬렉션을 처리하며, 각 레벨의 컬렉션 타입과
     * 최종 요소의 타입을 적절히 변환합니다.</p>
     * 
     * @param origin 변환할 원본 컬렉션
     * @param resultCollectionItemsList 결과 컬렉션의 각 레벨별 타입 정보
     * @param returnType 최종 요소의 타입
     * @return 변환된 컬렉션
     * @throws InvocationTargetException 컬렉션 생성자 호출 시 예외
     * @throws InstantiationException 컬렉션 인스턴스 생성 시 예외
     * @throws IllegalAccessException 컬렉션 생성자 접근 시 예외
     */
    @SuppressWarnings({"rawtypes", "ReassignedVariable", "unchecked"})
    public static Object convertCollectionValue(Object origin, List<CollectionItems> resultCollectionItemsList, Types returnType) 
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        
        if (origin == null) {
            return null;
        }
        
        if (!(origin instanceof Collection)) {
            throw new IllegalArgumentException("Origin must be a Collection type");
        }
        
        if (resultCollectionItemsList == null || resultCollectionItemsList.isEmpty()) {
            throw new IllegalArgumentException("resultCollectionItemsList cannot be null or empty");
        }
        
        Collection resultCollectionOfCurrent = resultCollectionItemsList.get(0).collectionConstructor.newInstance();
        Collection result = resultCollectionOfCurrent;
        ArrayDeque<Iterator> collectionIterators = new ArrayDeque<>();
        ArrayDeque<Collection> resultCollections = new ArrayDeque<>();
        int collectionItemIndex = 0;

        Iterator currentIterator = ((Collection<?>) origin).iterator();
        resultCollections.add(resultCollectionOfCurrent);
        collectionIterators.add(currentIterator);
        
        while (currentIterator.hasNext()) {
            Object next = currentIterator.next();
            
            if (next instanceof Collection) {
                // 중첩된 컬렉션 처리
                ++collectionItemIndex;
                if (collectionItemIndex >= resultCollectionItemsList.size()) {
                    throw new IllegalStateException("Collection nesting level exceeds expected depth");
                }
                
                Collection newCollection = resultCollectionItemsList.get(collectionItemIndex).collectionConstructor.newInstance();
                resultCollections.add(newCollection);
                resultCollectionOfCurrent.add(newCollection);
                resultCollectionOfCurrent = newCollection;
                currentIterator = ((Collection<?>) next).iterator();
                collectionIterators.add(currentIterator);
            } else {
                // 최종 요소 변환 및 추가
                Object convertedValue = convertValue(next, returnType);
                resultCollectionOfCurrent.add(convertedValue);
            }

            // 현재 반복자가 끝났을 때 상위 레벨로 돌아가기
            while (!currentIterator.hasNext()) {
                collectionIterators.removeLast();
                if (collectionIterators.isEmpty()) {
                    return result;
                }
                --collectionItemIndex;
                resultCollections.removeLast();
                resultCollectionOfCurrent = resultCollections.getLast();
                currentIterator = collectionIterators.getLast();
            }
        }

        return result;
    }
    
    /**
     * 주어진 Number가 이미 요청된 타입과 일치하는지 확인합니다.
     * 
     * @param number 확인할 Number 객체
     * @param returnType 요청된 타입
     * @return 타입이 일치하면 true, 그렇지 않으면 false
     */
    private static boolean isAlreadyCorrectType(Number number, Types returnType) {
        switch (returnType) {
            case BigDecimal:
                return number instanceof BigDecimal;
            case Double:
                return number instanceof Double;
            case Float:
                return number instanceof Float;
            case Long:
                return number instanceof Long;
            case Integer:
                return number instanceof Integer;
            case Short:
                return number instanceof Short;
            case Byte:
                return number instanceof Byte;
            default:
                return false;
        }
    }
    
    /**
     * 안전한 타입 변환을 수행합니다. 예외 발생 시 기본값을 반환합니다.
     * 
     * @param origin 변환할 원본 값
     * @param returnType 변환할 대상 타입
     * @param defaultValue 예외 발생 시 반환할 기본값
     * @return 변환된 값 또는 기본값
     */
    public static Object convertValueSafely(Object origin, Types returnType, Object defaultValue) {
        try {
            Object result = convertValue(origin, returnType);
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 문자열을 안전하게 타입 변환합니다. 예외 발생 시 기본값을 반환합니다.
     * 
     * @param origin 변환할 문자열
     * @param returnType 변환할 대상 타입
     * @param defaultValue 예외 발생 시 반환할 기본값
     * @return 변환된 값 또는 기본값
     */
    public static Object convertValueFromStringSafely(String origin, Types returnType, Object defaultValue) {
        try {
            Object result = convertValueFromString(origin, returnType);
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Number를 안전하게 타입 변환합니다. 예외 발생 시 기본값을 반환합니다.
     * 
     * @param origin 변환할 Number
     * @param returnType 변환할 대상 타입
     * @param defaultValue 예외 발생 시 반환할 기본값
     * @return 변환된 값 또는 기본값
     */
    public static Object convertValueFromNumberSafely(Number origin, Types returnType, Object defaultValue) {
        try {
            Object result = convertValueFromNumber(origin, returnType);
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 인스턴스화를 방지하는 private 생성자입니다.
     */
    private TypeConverter() {
        throw new AssertionError("TypeConverter cannot be instantiated");
    }
}
