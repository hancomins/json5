package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import com.hancomins.json5.options.WritingOptions;
import com.hancomins.json5.util.DataConverter;
import com.hancomins.json5.serializer.polymorphic.TypeInfoAnalyzer;
import com.hancomins.json5.serializer.polymorphic.PolymorphicDeserializer;

import java.util.*;

/**
 * 컬렉션 타입의 역직렬화를 담당하는 클래스
 * 
 * JSON5Serializer의 json5ArrayToList, json5ArrayToCollectionObject 메소드의 
 * 복잡한 로직을 분리하여 컬렉션 역직렬화만을 전담으로 처리합니다.
 */
public class CollectionDeserializer {
    
    private final TypeInfoAnalyzer typeInfoAnalyzer;
    private final PolymorphicDeserializer polymorphicDeserializer;
    
    public CollectionDeserializer() {
        this.typeInfoAnalyzer = new TypeInfoAnalyzer();
        this.polymorphicDeserializer = new PolymorphicDeserializer();
    }
    
    /**
     * JSON5Array를 List로 역직렬화
     * 
     * @param json5Array 역직렬화할 JSON5Array
     * @param valueType List의 요소 타입
     * @param writingOptions 작성 옵션
     * @param ignoreError 오류 무시 여부
     * @param defaultValue 기본값
     * @param <T> 요소 타입
     * @return 역직렬화된 List
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> deserializeToList(JSON5Array json5Array, Class<T> valueType, 
                                       WritingOptions writingOptions, boolean ignoreError, T defaultValue) {
        // 타입 검증
        validateValueType(valueType, ignoreError);
        
        Types types = Types.of(valueType);
        ArrayList<T> result = new ArrayList<>();
        
        for (int i = 0, n = json5Array.size(); i < n; ++i) {
            Object value = json5Array.get(i);
            if (value == null) {
                result.add(defaultValue);
                continue;
            }
            
            try {
                T convertedValue = convertValue(value, valueType, types, writingOptions, ignoreError, defaultValue);
                result.add(convertedValue);
            } catch (Exception e) {
                if (ignoreError) {
                    result.add(defaultValue);
                } else {
                    throw e;
                }
            }
        }
        
        return result;
    }
    
    /**
     * JSON5Array를 Collection으로 역직렬화 (Schema 기반)
     * 
     * @param json5Array 역직렬화할 JSON5Array
     * @param schemaArrayValue 스키마 배열 값
     * @param parent 부모 객체
     * @param context 역직렬화 컨텍스트
     */
    public void deserializeToCollection(JSON5Array json5Array, ISchemaArrayValue schemaArrayValue, 
                                      Object parent, DeserializationContext context) {
        deserializeToCollection(json5Array, schemaArrayValue, parent, context, null);
    }
    
    /**
     * JSON5Array를 Collection으로 역직렬화 (Schema 기반, OnObtainTypeValue 포함)
     * 
     * @param json5Array 역직렬화할 JSON5Array
     * @param schemaArrayValue 스키마 배열 값
     * @param parent 부모 객체
     * @param context 역직렬화 컨텍스트
     * @param onObtainTypeValue 타입 값 획득 함수
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void deserializeToCollection(JSON5Array json5Array, ISchemaArrayValue schemaArrayValue, 
                                      Object parent, DeserializationContext context, 
                                      OnObtainTypeValue onObtainTypeValue) {
        List<CollectionItems> collectionItems = schemaArrayValue.getCollectionItems();
        int collectionItemIndex = 0;
        final int collectionItemSize = collectionItems.size();
        
        if (collectionItemSize == 0) {
            return;
        }
        
        CollectionItems collectionItem = collectionItems.get(collectionItemIndex);
        ArrayList<ArrayDeserializeItem> arrayDeserializeItems = new ArrayList<>();
        ArrayDeserializeItem objectItem = new ArrayDeserializeItem(json5Array, collectionItem.newInstance());
        int end = objectItem.getEndIndex();
        arrayDeserializeItems.add(objectItem);
        
        for (int index = 0; index <= end; ++index) {
            objectItem.setArrayIndex(index);
            
            if (collectionItem.isGeneric() || collectionItem.isAbstractType()) {
                // 제네릭 또는 추상 타입 처리
                JSON5Object json5Object = objectItem.json5Array.getJSON5Object(index);
                Object object = null;
                
                // 1. 다형성 타입인지 먼저 확인
                Class<?> elementType = collectionItem.getValueClass();
                if (elementType != null && typeInfoAnalyzer.isPolymorphicType(elementType) && json5Object != null) {
                    try {
                        object = polymorphicDeserializer.deserialize(json5Object, elementType);
                    } catch (Exception e) {
                        // 다형성 역직렬화 실패 시 기존 방식으로 fallback
                        System.err.println("Polymorphic collection deserialization failed, falling back to @ObtainTypeValue: " + e.getMessage());
                    }
                }
                
                // 2. 다형성 역직렬화에 실패했거나 다형성 타입이 아닌 경우 기존 방식 사용
                if (object == null) {
                    object = onObtainTypeValue != null ? onObtainTypeValue.obtain(json5Object) : null;
                }
                
                objectItem.collectionObject.add(object);
            } else if (collectionItem.getValueClass() != null) {
                // 기본 값 타입 처리
                Object value = getValueFromJSON5Array(objectItem.json5Array, index, schemaArrayValue);
                objectItem.collectionObject.add(value);
            } else {
                // 중첩 배열 처리
                JSON5Array innerArray = objectItem.json5Array.getJSON5Array(index);
                if (innerArray == null) {
                    objectItem.collectionObject.add(null);
                } else {
                    collectionItem = collectionItems.get(++collectionItemIndex);
                    Collection newCollection = collectionItem.newInstance();
                    objectItem.collectionObject.add(newCollection);
                    ArrayDeserializeItem newArrayDeserializeItem = new ArrayDeserializeItem(innerArray, newCollection);
                    arrayDeserializeItems.add(newArrayDeserializeItem);
                    index = -1;
                    end = newArrayDeserializeItem.getEndIndex();
                    objectItem = newArrayDeserializeItem;
                }
            }
            
            // 현재 배열 처리 완료 시 상위 레벨로 이동
            while (index == end) {
                arrayDeserializeItems.remove(arrayDeserializeItems.size() - 1);
                if (arrayDeserializeItems.isEmpty()) {
                    break;
                }
                objectItem = arrayDeserializeItems.get(arrayDeserializeItems.size() - 1);
                index = objectItem.index;
                end = objectItem.arraySize - 1;
                collectionItem = collectionItems.get(--collectionItemIndex);
            }
        }
        
        // 최종 결과를 부모 객체에 설정
        schemaArrayValue.setValue(parent, objectItem.collectionObject);
    }
    
    /**
     * 값 타입 검증
     */
    private void validateValueType(Class<?> valueType, boolean ignoreError) {
        if (valueType.isPrimitive()) {
            if (ignoreError) {
                return;
            }
            throw new JSON5SerializerException("valueType is primitive type. valueType=" + valueType.getName());
        } else if (Collection.class.isAssignableFrom(valueType)) {
            if (ignoreError) {
                return;
            }
            throw new JSON5SerializerException("valueType is java.util.Collection type. Use a class that wraps your Collection. valueType=" + valueType.getName());
        } else if (Map.class.isAssignableFrom(valueType)) {
            if (ignoreError) {
                return;
            }
            throw new JSON5SerializerException("valueType is java.util.Map type. Use a class that wraps your Map. valueType=" + valueType.getName());
        } else if (valueType.isArray() && Types.ByteArray != Types.of(valueType)) {
            if (ignoreError) {
                return;
            }
            throw new JSON5SerializerException("valueType is Array type. ArrayType cannot be used. valueType=" + valueType.getName());
        }
    }
    
    /**
     * 값 변환
     */
    @SuppressWarnings("unchecked")
    private <T> T convertValue(Object value, Class<T> valueType, Types types, 
                             WritingOptions writingOptions, boolean ignoreError, T defaultValue) {
        if (Number.class.isAssignableFrom(valueType)) {
            try {
                Number no = DataConverter.toBoxingNumberOfType(value, (Class<? extends Number>) valueType);
                return (T) no;
            } catch (NumberFormatException e) {
                if (ignoreError) {
                    return defaultValue;
                }
                throw new JSON5SerializerException("valueType is Number type. But value is not Number type. valueType=" + valueType.getName());
            }
        } else if (Boolean.class == valueType) {
            if (value.getClass() == Boolean.class) {
                return (T) value;
            } else {
                return "true".equals(value.toString()) ? (T) Boolean.TRUE : (T) Boolean.FALSE;
            }
        } else if (Character.class == valueType) {
            try {
                if (value.getClass() == Character.class) {
                    return (T) value;
                } else {
                    return (T) (Character) DataConverter.toChar(value);
                }
            } catch (NumberFormatException e) {
                if (ignoreError) {
                    return defaultValue;
                }
                throw new JSON5SerializerException("valueType is Character type. But value is not Character type. valueType=" + valueType.getName());
            }
        } else if (valueType == String.class) {
            if (writingOptions != null && value instanceof JSON5Element) {
                return (T) ((JSON5Element) value).toString(writingOptions);
            } else {
                return (T) value.toString();
            }
        } else if (value instanceof JSON5Object && JSON5Serializer.serializable(valueType)) {
            try {
                // ObjectDeserializer를 사용하여 역직렬화
                ObjectDeserializer objectDeserializer = new ObjectDeserializer();
                Object target = valueType.getDeclaredConstructor().newInstance();
                value = objectDeserializer.deserialize((JSON5Object) value, target);
            } catch (Exception e) {
                if (ignoreError) {
                    return defaultValue;
                }
                throw new JSON5SerializerException("Failed to deserialize object. valueType=" + valueType.getName(), e);
            }
            return (T) value;
        }
        
        return defaultValue;
    }
    
    /**
     * JSON5Array에서 특정 인덱스의 값을 타입에 맞게 추출
     */
    private Object getValueFromJSON5Array(JSON5Array json5Array, int index, ISchemaArrayValue schemaArrayValue) {
        switch (schemaArrayValue.getEndpointValueType()) {
            case Byte:
                return json5Array.getByte(index);
            case Short:
                return json5Array.getShort(index);
            case Integer:
                return json5Array.getInt(index);
            case Long:
                return json5Array.getLong(index);
            case Float:
                return json5Array.getFloat(index);
            case Double:
                return json5Array.getDouble(index);
            case Boolean:
                return json5Array.getBoolean(index);
            case Character:
                return json5Array.getChar(index, '\0');
            case String:
                return json5Array.getString(index);
            case JSON5Array:
                return json5Array.getJSON5Array(index);
            case JSON5Object:
                return json5Array.getJSON5Object(index);
            case Object:
                JSON5Object json5Object = json5Array.getJSON5Object(index);
                if (json5Object != null) {
                    Object target = schemaArrayValue.getObjectValueTypeElement().newInstance();
                    ObjectDeserializer objectDeserializer = new ObjectDeserializer();
                    return objectDeserializer.deserialize(json5Object, target);
                }
                break;
        }
        return null;
    }
    
    /**
     * OnObtainTypeValue 함수형 인터페이스
     */
    @FunctionalInterface
    public interface OnObtainTypeValue {
        Object obtain(Object target);
    }
    
    /**
     * 배열 역직렬화 스택 아이템
     */
    @SuppressWarnings("rawtypes")
    private static class ArrayDeserializeItem {
        final JSON5Array json5Array;
        final Collection collectionObject;
        int index = 0;
        int arraySize = 0;
        
        private ArrayDeserializeItem(JSON5Array json5Array, Collection collection) {
            this.json5Array = json5Array;
            this.arraySize = json5Array.size();
            this.collectionObject = collection;
        }
        
        private int getEndIndex() {
            return arraySize - 1;
        }
        
        /**
         * 역직렬화에서 사용됨.
         */
        private void setArrayIndex(int index) {
            this.index = index;
        }
    }
}
