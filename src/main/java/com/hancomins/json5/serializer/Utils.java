package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.math.BigDecimal;

public class Utils {

    static Class<?> primitiveTypeToBoxedType(Class<?> primitiveType) {
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


    @SuppressWarnings({"rawtypes", "ReassignedVariable", "unchecked"})
    static Object convertCollectionValue(Object origin, List<CollectionItems> resultCollectionItemsList, Types returnType) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if(origin == null) {
            return null;
        }
        Collection resultCollectionOfCurrent = resultCollectionItemsList.get(0).collectionConstructor.newInstance();
        Collection result = resultCollectionOfCurrent;
        ArrayDeque<Iterator> collectionIterators = new ArrayDeque<>();
        ArrayDeque<Collection> resultCollections = new ArrayDeque<>();
        int collectionItemIndex = 0;

        Iterator currentIterator = ((Collection<?>)origin).iterator();
        resultCollections.add(resultCollectionOfCurrent);
        collectionIterators.add(currentIterator);
        while(currentIterator.hasNext()) {
            Object next = currentIterator.next();
            if(next instanceof Collection) {
                ++collectionItemIndex;
                Collection newCollection = resultCollectionItemsList.get(collectionItemIndex).collectionConstructor.newInstance();
                resultCollections.add(newCollection);
                resultCollectionOfCurrent.add(newCollection);
                resultCollectionOfCurrent = newCollection;
                currentIterator = ((Collection<?>)next).iterator();
                collectionIterators.add(currentIterator);
            } else {
                resultCollectionOfCurrent.add(convertValue(next,returnType));
            }

            while(!currentIterator.hasNext()) {
                collectionIterators.removeLast();
                if(collectionIterators.isEmpty()) {
                    return result;
                }
                --collectionItemIndex;
                resultCollections.removeLast();
                resultCollectionOfCurrent =  resultCollections.getLast();
                currentIterator = collectionIterators.getLast();
            }

        }

        return result;

    }

    static Object convertValue(Object origin, Types returnType) {
        try {
            if(origin instanceof String) {
                return convertValueFromString((String)origin,returnType);
            } else if(origin instanceof Number) {
                return convertValueFromNumber((Number)origin,returnType);
            }

        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    static Object convertValueFromString(String origin, Types returnType) {
        if(origin == null) {
            return null;
        }
        if(returnType == Types.String) {
            return origin;
        } else if(returnType == Types.Byte) {
            return Byte.valueOf(origin);
        } else if(returnType == Types.Short) {
            return Short.valueOf(origin);
        } else if(returnType == Types.Integer) {
            return Integer.valueOf(origin);
        } else if(returnType == Types.Long) {
            return Long.valueOf(origin);
        } else if(returnType == Types.Float) {
            return Float.valueOf(origin);
        } else if(returnType == Types.Double) {
            return Double.valueOf(origin);
        } else if(returnType == Types.Character) {
            return origin.charAt(0);
        } else if(returnType == Types.Boolean) {
            return Boolean.valueOf(origin);
        } else if(returnType == Types.BigDecimal) {
            return new java.math.BigDecimal(origin);
        } else if(returnType == Types.BigInteger) {
            return new java.math.BigInteger(origin);
        }


        return null;

    }

    static Object convertValueFromNumber(Number origin, Types returnType) {
        if(origin == null) {
            return null;
        }
        if(origin instanceof BigDecimal && returnType == Types.BigDecimal) {
            return origin;
        } else if(origin instanceof Double && returnType == Types.Double) {
            return origin;
        } else if(origin instanceof Float && returnType == Types.Float) {
            return origin;
        } else if(origin instanceof Long && returnType == Types.Long) {
            return origin;
        } else if(origin instanceof Integer && returnType == Types.Integer) {
            return origin;
        } else if(origin instanceof Short && returnType == Types.Short) {
            return origin;
        } else if(origin instanceof Byte && returnType == Types.Byte) {
            return origin;
        }

        if(returnType == Types.Byte) {
            return origin.byteValue();
        } else if(returnType == Types.Short) {
            return origin.shortValue();
        } else if(returnType == Types.Integer) {
            return origin.intValue();
        } else if(returnType == Types.Long) {
            return origin.longValue();
        } else if(returnType == Types.Float) {
            return origin.floatValue();
        } else if(returnType == Types.Double) {
            return origin.doubleValue();
        } else if(returnType == Types.Character) {
            return (char)origin.intValue();
        } else if(returnType == Types.Boolean) {
            return origin.intValue() != 0;
        } else if(returnType == Types.BigDecimal) {
            return new java.math.BigDecimal(origin.toString());
        } else if(returnType == Types.BigInteger) {
            return new java.math.BigInteger(origin.toString());
        } else if(returnType == Types.String) {
            return origin.toString();
        } else if(returnType == Types.ByteArray) {
            return new byte[]{origin.byteValue()};
        }
        return null;
    }

    static Object optFrom(JSON5Element json5, Object key, Types valueType) {
        boolean isArrayType = json5 instanceof JSON5Array;
        if(isArrayType && ((JSON5Array)json5).isNull((int)key)) {
            return null;
        } else if(!isArrayType && ((JSON5Object)json5).isNull((String)key)) {
            return null;
        }
        if(Types.Boolean == valueType) {
            return isArrayType ? ((JSON5Array) json5).optBoolean((int)key) : ((JSON5Object)json5).optBoolean((String)key);
        } else if(Types.Byte == valueType) {
            return  isArrayType ? ((JSON5Array) json5).optByte((int)key) : ((JSON5Object)json5).optByte((String)key);
        } else if(Types.Character == valueType) {
            return  isArrayType ? ((JSON5Array) json5).optChar((int)key, '\0') : ((JSON5Object)json5).optChar((String)key, '\0');
        } else if(Types.Short == valueType) {
            return  isArrayType ? ((JSON5Array) json5).optShort((int)key) : ((JSON5Object)json5).optShort((String)key);
        } else if(Types.Integer == valueType) {
            return  isArrayType ? ((JSON5Array) json5).optInt((int)key) : ((JSON5Object)json5).optInt((String)key);
        } else if(Types.Float == valueType) {
            return  isArrayType ? ((JSON5Array) json5).optFloat((int)key) : ((JSON5Object)json5).optFloat((String)key);
        } else if(Types.Double == valueType) {
            return  isArrayType ? ((JSON5Array) json5).optDouble((int)key) : ((JSON5Object)json5).optDouble((String)key);
        } else if(Types.String == valueType) {
            return  isArrayType ? ((JSON5Array) json5).optString((int)key) : ((JSON5Object)json5).optString((String)key);
        }  else if(Types.ByteArray == valueType) {
            return  isArrayType ? ((JSON5Array) json5).optByteArray((int)key) : ((JSON5Object)json5).optByteArray((String)key);
        } else {
            return  isArrayType ? ((JSON5Array) json5).opt((int)key) : ((JSON5Object)json5).opt((String)key);
        }
    }

}
