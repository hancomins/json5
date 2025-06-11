package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import com.hancomins.json5.util.DataConverter;

import java.util.*;

/**
 * 복합 객체의 역직렬화를 담당하는 클래스
 * 
 * JSON5Serializer의 fromJSON5Object 메소드의 복잡한 로직을 분리하여
 * 객체 역직렬화만을 전담으로 처리합니다.
 */
public class ObjectDeserializer {
    
    /**
     * JSON5Object를 대상 객체로 역직렬화
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param targetObject 대상 객체
     * @param <T> 반환 타입
     * @return 역직렬화된 객체
     */
    public <T> T deserialize(JSON5Object json5Object, T targetObject) {
        return deserialize(json5Object, targetObject, null);
    }
    
    /**
     * JSON5Object를 대상 객체로 역직렬화 (컨텍스트 포함)
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param targetObject 대상 객체
     * @param context 역직렬화 컨텍스트 (null 가능)
     * @param <T> 반환 타입
     * @return 역직렬화된 객체
     */
    public <T> T deserialize(JSON5Object json5Object, T targetObject, DeserializationContext context) {
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(targetObject.getClass());
        SchemaObjectNode schemaRoot = typeSchema.getSchemaObjectNode();
        
        // 컨텍스트가 제공되지 않았으면 기본 컨텍스트 생성
        if (context == null) {
            context = new DeserializationContext(targetObject, json5Object, typeSchema);
        }
        
        // 중첩 구조 처리를 위한 스택
        ArrayDeque<ObjectDeserializeItem> deserializeStack = new ArrayDeque<>();
        Iterator<Object> iter = schemaRoot.keySet().iterator();
        SchemaObjectNode schemaNode = schemaRoot;
        JSON5Element currentElement = json5Object;
        
        ObjectDeserializeItem currentItem = new ObjectDeserializeItem(iter, schemaNode, currentElement);
        deserializeStack.add(currentItem);
        
        // 스키마 기반 역직렬화 수행
        while (iter.hasNext()) {
            Object key = iter.next();
            ISchemaNode node = schemaNode.get(key);
            JSON5Element parentJson5Element = currentElement;
            
            if (node instanceof SchemaElementNode) {
                // 중첩 객체 또는 배열 처리
                handleSchemaElementNode(key, node, currentElement, schemaNode, context, deserializeStack);
                
                // 스택 업데이트
                if (!deserializeStack.isEmpty()) {
                    ObjectDeserializeItem topItem = deserializeStack.peekLast();
                    iter = topItem.keyIterator;
                    schemaNode = topItem.schemaNode;
                    currentElement = topItem.currentElement;
                }
            } else if (node instanceof SchemaValueAbs && ((SchemaValueAbs) node).types() != Types.Object) {
                // 일반 필드 처리
                handleSchemaValueNode(key, node, currentElement, context);
            }
            
            // 현재 레벨이 완료되면 상위 레벨로 이동
            while (!iter.hasNext() && !deserializeStack.isEmpty()) {
                deserializeStack.removeLast();
                if (!deserializeStack.isEmpty()) {
                    ObjectDeserializeItem item = deserializeStack.peekLast();
                    iter = item.keyIterator;
                    schemaNode = item.schemaNode;
                    currentElement = item.currentElement;
                }
            }
        }
        
        return targetObject;
    }
    
    /**
     * SchemaElementNode 처리 (중첩 객체/배열)
     */
    private void handleSchemaElementNode(Object key, ISchemaNode node, JSON5Element currentElement,
                                       SchemaObjectNode schemaNode, DeserializationContext context,
                                       ArrayDeque<ObjectDeserializeItem> deserializeStack) {
        
        boolean nullValue = false;
        JSON5Element childElement = getChildElement((SchemaElementNode) node, currentElement, key);
        
        // null 값 확인
        if (key instanceof String) {
            JSON5Object parentObject = (JSON5Object) currentElement;
            if (childElement == null) {
                if (parentObject.isNull((String) key)) {
                    nullValue = true;
                } else {
                    return;
                }
            }
        } else {
            JSON5Array parentArray = (JSON5Array) currentElement;
            int index = (Integer) key;
            if (childElement == null) {
                if (parentArray.size() <= index || parentArray.isNull(index)) {
                    nullValue = true;
                } else {
                    return;
                }
            }
        }
        
        // 부모 객체들 생성 및 등록
        SchemaObjectNode childSchemaNode = (SchemaObjectNode) node;
        List<SchemaValueAbs> parentSchemaFieldList = childSchemaNode.getParentSchemaFieldList();
        
        for (SchemaValueAbs parentSchemaField : parentSchemaFieldList) {
            getOrCreateParentObject(parentSchemaField, context, nullValue, currentElement);
        }
        
        // 다음 레벨로 이동
        Iterator<Object> childIter = childSchemaNode.keySet().iterator();
        ObjectDeserializeItem childItem = new ObjectDeserializeItem(childIter, childSchemaNode, childElement);
        deserializeStack.add(childItem);
    }
    
    /**
     * SchemaValueAbs 처리 (일반 필드)
     */
    private void handleSchemaValueNode(Object key, ISchemaNode node, JSON5Element currentElement,
                                     DeserializationContext context) {
        SchemaValueAbs schemaField = (SchemaValueAbs) node;
        SchemaValueAbs parentField = schemaField.getParentField();
        
        if (currentElement != null) {
            Object obj = getOrCreateParentObject(parentField, context, currentElement);
            setValueFromJSON5Element(obj, schemaField, currentElement, key, context);
        }
    }
    
    /**
     * JSON5Element에서 자식 요소 추출
     */
    private JSON5Element getChildElement(SchemaElementNode schemaElementNode, JSON5Element json5Element, Object key) {
        if (key instanceof String) {
            JSON5Object json5Object = (JSON5Object) json5Element;
            return schemaElementNode instanceof SchemaArrayNode ? 
                   json5Object.getJSON5Array((String) key) : 
                   json5Object.getJSON5Object((String) key);
        } else {
            JSON5Array json5Array = (JSON5Array) json5Element;
            return schemaElementNode instanceof SchemaArrayNode ?
                   json5Array.getJSON5Array((int) key) :
                   json5Array.getJSON5Object((int) key);
        }
    }
    
    /**
     * 부모 객체 생성 또는 조회
     */
    private Object getOrCreateParentObject(SchemaValueAbs parentSchemaField, DeserializationContext context, 
                                         JSON5Element json5Element) {
        return getOrCreateParentObject(parentSchemaField, context, false, json5Element);
    }
    
    /**
     * 부모 객체 생성 또는 조회 (null 설정 포함)
     */
    private Object getOrCreateParentObject(SchemaValueAbs parentSchemaField, DeserializationContext context,
                                         boolean setNull, JSON5Element json5Element) {
        if (parentSchemaField == null) {
            return context.getRootObject();
        }
        
        int id = parentSchemaField.getId();
        Object parent = context.getParentObject(id);
        if (parent != null) {
            return parent;
        }
        
        // 계보 추적하여 상위부터 생성
        ArrayList<SchemaValueAbs> pedigreeList = new ArrayList<>();
        SchemaValueAbs current = parentSchemaField;
        while (current != null) {
            pedigreeList.add(current);
            current = current.getParentField();
        }
        Collections.reverse(pedigreeList);
        
        parent = context.getRootObject();
        SchemaValueAbs last = pedigreeList.get(pedigreeList.size() - 1);
        
        for (SchemaValueAbs schemaField : pedigreeList) {
            int parentId = schemaField.getId();
            Object child = context.getParentObject(parentId);
            
            if (setNull && child == null && schemaField == last) {
                schemaField.setValue(parent, null);
            } else if (!setNull && child == null) {
                if (schemaField instanceof ObtainTypeValueInvokerGetter) {
                    ObtainTypeValueInvoker obtainTypeValueInvoker = 
                        ((ObtainTypeValueInvokerGetter) schemaField).getObtainTypeValueInvoker();
                    if (obtainTypeValueInvoker != null) {
                        OnObtainTypeValue onObtainTypeValue = 
                            createOnObtainTypeValue((ObtainTypeValueInvokerGetter) schemaField, parent, context);
                        child = onObtainTypeValue.obtain(json5Element);
                    }
                }
                if (child == null) {
                    child = schemaField.newInstance();
                }
                context.putParentObject(parentId, child);
                schemaField.setValue(parent, child);
            }
            parent = child;
        }
        
        return parent;
    }
    
    /**
     * JSON5Element에서 값을 추출하여 객체에 설정
     */
    private void setValueFromJSON5Element(Object parent, SchemaValueAbs schemaField, JSON5Element json5Element,
                                        Object key, DeserializationContext context) {
        List<SchemaValueAbs> schemaValueAbsList = schemaField.getAllSchemaValueList();
        for (SchemaValueAbs schemaValueAbs : schemaValueAbsList) {
            setValueFromJSON5ElementSingle(parent, schemaValueAbs, json5Element, key, context);
        }
    }
    
    /**
     * 단일 스키마 값 설정
     */
    private void setValueFromJSON5ElementSingle(Object parent, SchemaValueAbs schemaField, JSON5Element json5Element,
                                              Object key, DeserializationContext context) {
        Types valueType = schemaField.getType();
        boolean isArrayType = json5Element instanceof JSON5Array;
        
        if (Types.isSingleType(valueType)) {
            // 기본 타입 처리
            Object valueObj = JSON5ElementExtractor.getFrom(json5Element, key, valueType);
            // 키가 존재하지 않으면 값을 설정하지 않음 (기본값 유지)
            if (JSON5ElementExtractor.isMissingKey(valueObj)) {
                return;
            }
            schemaField.setValue(parent, valueObj);
        } else if ((Types.AbstractObject == valueType || Types.GenericType == valueType) && 
                   schemaField instanceof ObtainTypeValueInvokerGetter) {
            // 추상/제네릭 타입 처리
            handleAbstractOrGenericType(parent, schemaField, json5Element, key, context);
        } else if (Types.Collection == valueType) {
            // 컬렉션 타입 처리
            handleCollectionType(parent, schemaField, json5Element, key, context, isArrayType);
        } else if (Types.Object == valueType) {
            // 객체 타입 처리
            handleObjectType(parent, schemaField, json5Element, key, isArrayType);
        } else if (Types.Map == valueType) {
            // 맵 타입 처리
            handleMapType(parent, schemaField, json5Element, key, context, isArrayType);
        } else if (Types.JSON5Object == valueType) {
            // JSON5Object 타입 처리
            if (JSON5ElementExtractor.hasKey(json5Element, key)) {
                JSON5Object value = isArrayType ? 
                    ((JSON5Array) json5Element).getJSON5Object((int) key) : 
                    ((JSON5Object) json5Element).getJSON5Object((String) key);
                schemaField.setValue(parent, value);
            }
            // 키가 없으면 아무것도 하지 않음 (기본값 유지)
        } else if (Types.JSON5Array == valueType) {
            // JSON5Array 타입 처리
            if (JSON5ElementExtractor.hasKey(json5Element, key)) {
                JSON5Array value = isArrayType ? 
                    ((JSON5Array) json5Element).getJSON5Array((int) key) : 
                    ((JSON5Object) json5Element).getJSON5Array((String) key);
                schemaField.setValue(parent, value);
            }
            // 키가 없으면 아무것도 하지 않음 (기본값 유지)
        } else if (Types.JSON5Element == valueType) {
            // JSON5Element 타입 처리
            if (JSON5ElementExtractor.hasKey(json5Element, key)) {
                Object value = isArrayType ? 
                    ((JSON5Array) json5Element).get((int) key) : 
                    ((JSON5Object) json5Element).get((String) key);
                if (value instanceof JSON5Element) {
                    schemaField.setValue(parent, value);
                } else {
                    schemaField.setValue(parent, null);
                }
            }
            // 키가 없으면 아무것도 하지 않음 (기본값 유지)
        } else {
            // 기타 타입은 키가 존재할 때만 null로 설정
            if (JSON5ElementExtractor.hasKey(json5Element, key)) {
                try {
                    schemaField.setValue(parent, null);
                } catch (Exception ignored) {
                    // 무시
                }
            }
        }
    }
    
    /**
     * 추상/제네릭 타입 처리
     */
    private void handleAbstractOrGenericType(Object parent, SchemaValueAbs schemaField, JSON5Element json5Element,
                                           Object key, DeserializationContext context) {
        Object val = JSON5ElementExtractor.getFrom(json5Element, key, schemaField.getType());
        
        // 키가 존재하지 않으면 값을 설정하지 않음 (기본값 유지)
        if (JSON5ElementExtractor.isMissingKey(val)) {
            return;
        }
        
        Object obj = createOnObtainTypeValue((ObtainTypeValueInvokerGetter) schemaField, parent, context).obtain(val);
        
        if (obj == null) {
            obj = schemaField.newInstance();
            obj = dynamicCasting(obj, val);
        }
        schemaField.setValue(parent, obj);
    }
    
    /**
     * 컬렉션 타입 처리
     */
    private void handleCollectionType(Object parent, SchemaValueAbs schemaField, JSON5Element json5Element,
                                    Object key, DeserializationContext context, boolean isArrayType) {
        // 키가 존재하지 않으면 아무것도 하지 않음 (기본값 유지)
        if (!JSON5ElementExtractor.hasKey(json5Element, key)) {
            return;
        }
        
        JSON5Array json5Array = isArrayType ? 
            ((JSON5Array) json5Element).getJSON5Array((int) key) : 
            ((JSON5Object) json5Element).getJSON5Array((String) key);
            
        if (json5Array != null) {
            OnObtainTypeValue onObtainTypeValue = null;
            boolean isGenericOrAbsType = ((ISchemaArrayValue) schemaField).isGenericTypeValue() || 
                                       ((ISchemaArrayValue) schemaField).isAbstractType();
            if (isGenericOrAbsType) {
                onObtainTypeValue = createOnObtainTypeValue((ObtainTypeValueInvokerGetter) schemaField, parent, context);
            }
            
            // CollectionDeserializer에 위임
            CollectionDeserializer collectionDeserializer = new CollectionDeserializer();
            // OnObtainTypeValue 어댑터 사용
            CollectionDeserializer.OnObtainTypeValue collectionOnObtainTypeValue = null;
            if (onObtainTypeValue != null) {
                collectionOnObtainTypeValue = onObtainTypeValue::obtain;
            }
            collectionDeserializer.deserializeToCollection(json5Array, (ISchemaArrayValue) schemaField, parent, context, collectionOnObtainTypeValue);
        } else if (isArrayType ? ((JSON5Array) json5Element).isNull((int) key) : ((JSON5Object) json5Element).isNull((String) key)) {
            try {
                schemaField.setValue(parent, null);
            } catch (Exception ignored) {
                // 무시
            }
        }
    }
    
    /**
     * 객체 타입 처리
     */
    private void handleObjectType(Object parent, SchemaValueAbs schemaField, JSON5Element json5Element,
                                Object key, boolean isArrayType) {
        // 키가 존재하지 않으면 아무것도 하지 않음 (기본값 유지)
        if (!JSON5ElementExtractor.hasKey(json5Element, key)) {
            return;
        }
        
        JSON5Object json5Obj = isArrayType ? 
            ((JSON5Array) json5Element).getJSON5Object((int) key) : 
            ((JSON5Object) json5Element).getJSON5Object((String) key);
            
        if (json5Obj != null) {
            Object target = schemaField.newInstance();
            // 재귀적으로 역직렬화
            ObjectDeserializer deserializer = new ObjectDeserializer();
            deserializer.deserialize(json5Obj, target);
            schemaField.setValue(parent, target);
        } else if (isArrayType ? ((JSON5Array) json5Element).isNull((int) key) : ((JSON5Object) json5Element).isNull((String) key)) {
            schemaField.setValue(parent, null);
        }
    }
    
    /**
     * 맵 타입 처리
     */
    private void handleMapType(Object parent, SchemaValueAbs schemaField, JSON5Element json5Element,
                             Object key, DeserializationContext context, boolean isArrayType) {
        // 키가 존재하지 않으면 아무것도 하지 않음 (기본값 유지)
        if (!JSON5ElementExtractor.hasKey(json5Element, key)) {
            return;
        }
        
        JSON5Object json5Obj = isArrayType ? 
            ((JSON5Array) json5Element).getJSON5Object((int) key) : 
            ((JSON5Object) json5Element).getJSON5Object((String) key);
            
        if (json5Obj != null) {
            Object target = schemaField.newInstance();
            Class<?> type = ((ISchemaMapValue) schemaField).getElementType();
            boolean isGenericOrAbstract = ((ISchemaMapValue) schemaField).isGenericValue() || 
                                        ((ISchemaMapValue) schemaField).isAbstractType();
            OnObtainTypeValue onObtainTypeValue = null;
            if (isGenericOrAbstract) {
                onObtainTypeValue = createOnObtainTypeValue((ObtainTypeValueInvokerGetter) schemaField, parent, context);
            }
            
            // MapDeserializer에 위임
            MapDeserializer mapDeserializer = new MapDeserializer();
            // OnObtainTypeValue 어댑터 사용
            MapDeserializer.OnObtainTypeValue mapOnObtainTypeValue = null;
            if (onObtainTypeValue != null) {
                mapOnObtainTypeValue = onObtainTypeValue::obtain;
            }
            mapDeserializer.deserialize((Map<?, ?>) target, json5Obj, type, context, mapOnObtainTypeValue);
            schemaField.setValue(parent, target);
        } else if (isArrayType ? ((JSON5Array) json5Element).isNull((int) key) : ((JSON5Object) json5Element).isNull((String) key)) {
            schemaField.setValue(parent, null);
        }
    }
    
    /**
     * OnObtainTypeValue 생성
     */
    private OnObtainTypeValue createOnObtainTypeValue(ObtainTypeValueInvokerGetter obtainTypeValueInvokerGetter,
                                                    Object parent, DeserializationContext context) {
        return (json5ObjectOrValue) -> {
            ObtainTypeValueInvoker invoker = obtainTypeValueInvokerGetter.getObtainTypeValueInvoker();
            if (invoker == null) {
                if (obtainTypeValueInvokerGetter.isIgnoreError()) {
                    return null;
                }
                throw new JSON5SerializerException("To deserialize a generic, abstract or interface type you must have a @ObtainTypeValue annotated method. target=" + 
                                                 obtainTypeValueInvokerGetter.targetPath());
            }
            try {
                Object obj = invoker.obtain(parent, 
                    json5ObjectOrValue instanceof JSON5Object ? (JSON5Object) json5ObjectOrValue : 
                    new JSON5Object().put("$value", json5ObjectOrValue), 
                    context.getRootJson5Object());
                if (obj != null && invoker.isDeserializeAfter()) {
                    obj = dynamicCasting(obj, json5ObjectOrValue);
                }
                return obj;
            } catch (RuntimeException e) {
                if (obtainTypeValueInvokerGetter.isIgnoreError()) {
                    return null;
                }
                throw e;
            }
        };
    }
    
    /**
     * 동적 캐스팅 수행
     */
    private Object dynamicCasting(Object value, Object realValue) {
        if (value == null) return null;
        Class<?> valueClass = value.getClass();
        Types valueType = Types.of(value.getClass());
        
        if (valueClass.isEnum()) {
            try {
                @SuppressWarnings("unchecked")
                Enum<?> enumValue = Enum.valueOf((Class<? extends Enum>) valueClass, realValue.toString());
                return enumValue;
            } catch (Exception e) {
                return null;
            }
        }
        
        if (Types.isSingleType(valueType) || Types.isJSON5Type(valueType)) {
            return DataConverter.convertValue(valueClass, realValue);
        } else if (Types.Object == valueType) {
            JSON5Object json5Obj = realValue instanceof JSON5Object ? (JSON5Object) realValue : null;
            if (json5Obj != null) {
                ObjectDeserializer deserializer = new ObjectDeserializer();
                deserializer.deserialize(json5Obj, value);
                return value;
            }
        }
        return null;
    }
    
    /**
     * OnObtainTypeValue 함수형 인터페이스 (공통 사용)
     */
    @FunctionalInterface
    private interface OnObtainTypeValue {
        Object obtain(Object target);
    }
    
    /**
     * 역직렬화 스택 아이템
     */
    private static class ObjectDeserializeItem {
        final Iterator<Object> keyIterator;
        final SchemaObjectNode schemaNode;
        final JSON5Element currentElement;
        
        ObjectDeserializeItem(Iterator<Object> keyIterator, SchemaObjectNode schemaNode, JSON5Element currentElement) {
            this.keyIterator = keyIterator;
            this.schemaNode = schemaNode;
            this.currentElement = currentElement;
        }
    }
}
