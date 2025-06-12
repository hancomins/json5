package com.hancomins.json5.serializer;

/**
 * Primitive 타입과 Boxed 타입 간의 변환을 담당하는 유틸리티 클래스입니다.
 * 
 * <p>Java의 primitive 타입(int, long, float 등)을 해당하는 Wrapper 클래스
 * (Integer, Long, Float 등)로 변환하는 기능을 제공합니다. 이는 리플렉션을 
 * 사용할 때 primitive 타입을 직접 처리할 수 없는 경우에 유용합니다.</p>
 * 
 * <h3>사용 예제:</h3>
 * <pre>{@code
 * Class<?> intType = int.class;
 * Class<?> integerType = PrimitiveTypeConverter.primitiveTypeToBoxedType(intType);
 * // integerType은 Integer.class가 됩니다.
 * }</pre>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public final class PrimitiveTypeConverter {
    
    /**
     * Primitive 타입을 해당하는 Boxed 타입으로 변환합니다.
     * 
     * <p>Java의 8개 primitive 타입(byte, short, int, long, float, double, boolean, char)과
     * void 타입을 해당하는 Wrapper 클래스로 변환합니다. primitive 타입이 아닌 경우
     * 원래 타입을 그대로 반환합니다.</p>
     * 
     * @param primitiveType 변환할 primitive 타입 (null이 아니어야 함)
     * @return 해당하는 Boxed 타입, primitive 타입이 아닌 경우 원래 타입
     * @throws IllegalArgumentException primitiveType이 null인 경우
     */
    public static Class<?> primitiveTypeToBoxedType(Class<?> primitiveType) {
        if (primitiveType == null) {
            throw new IllegalArgumentException("primitiveType cannot be null");
        }
        
        if (primitiveType == int.class) {
            return Integer.class;
        } else if (primitiveType == long.class) {
            return Long.class;
        } else if (primitiveType == float.class) {
            return Float.class;
        } else if (primitiveType == double.class) {
            return Double.class;
        } else if (primitiveType == boolean.class) {
            return Boolean.class;
        } else if (primitiveType == char.class) {
            return Character.class;
        } else if (primitiveType == byte.class) {
            return Byte.class;
        } else if (primitiveType == short.class) {
            return Short.class;
        } else if (primitiveType == void.class) {
            return Void.class;
        } else {
            return primitiveType;
        }
    }
    
    /**
     * 주어진 타입이 primitive 타입인지 확인합니다.
     * 
     * @param type 확인할 타입
     * @return primitive 타입이면 true, 그렇지 않으면 false
     */
    public static boolean isPrimitiveType(Class<?> type) {
        return type != null && type.isPrimitive();
    }
    
    /**
     * 주어진 타입이 primitive wrapper 타입인지 확인합니다.
     * 
     * @param type 확인할 타입
     * @return primitive wrapper 타입이면 true, 그렇지 않으면 false
     */
    public static boolean isPrimitiveWrapperType(Class<?> type) {
        if (type == null) {
            return false;
        }
        
        return type == Integer.class || type == Long.class || type == Float.class ||
               type == Double.class || type == Boolean.class || type == Character.class ||
               type == Byte.class || type == Short.class || type == Void.class;
    }
    
    /**
     * Boxed 타입을 해당하는 primitive 타입으로 변환합니다.
     * 
     * @param boxedType 변환할 Boxed 타입
     * @return 해당하는 primitive 타입, Boxed 타입이 아닌 경우 원래 타입
     */
    public static Class<?> boxedTypeToPrimitiveType(Class<?> boxedType) {
        if (boxedType == null) {
            throw new IllegalArgumentException("boxedType cannot be null");
        }
        
        if (boxedType == Integer.class) {
            return int.class;
        } else if (boxedType == Long.class) {
            return long.class;
        } else if (boxedType == Float.class) {
            return float.class;
        } else if (boxedType == Double.class) {
            return double.class;
        } else if (boxedType == Boolean.class) {
            return boolean.class;
        } else if (boxedType == Character.class) {
            return char.class;
        } else if (boxedType == Byte.class) {
            return byte.class;
        } else if (boxedType == Short.class) {
            return short.class;
        } else if (boxedType == Void.class) {
            return void.class;
        } else {
            return boxedType;
        }
    }
    
    /**
     * 인스턴스화를 방지하는 private 생성자입니다.
     */
    private PrimitiveTypeConverter() {
        throw new AssertionError("PrimitiveTypeConverter cannot be instantiated");
    }
}
