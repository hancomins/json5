package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import java.util.*;

/**
 * 객체 타입의 직렬화를 담당하는 클래스입니다.
 * 
 * <p>현재 구현에서는 안정성을 위해 기존 JSON5Serializer의 
 * serializeTypeElement_deprecated 메소드를 직접 사용합니다.</p>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class ObjectSerializer {
    
    /**
     * ObjectSerializer를 생성합니다.
     */
    public ObjectSerializer() {
        // 기본 생성자
    }
    
    /**
     * TypeSchema를 기반으로 객체를 직렬화합니다.
     * 
     * <p>안정성을 위해 기존 JSON5Serializer의 deprecated 메소드를 사용합니다.</p>
     * 
     * @param typeSchema 타입 스키마 정보
     * @param rootObject 직렬화할 루트 객체
     * @param rootJSON5Object 결과 JSON5Object (사용되지 않음)
     * @param context 직렬화 컨텍스트 (사용되지 않음)
     * @return 직렬화된 JSON5Object
     */
    public JSON5Object serializeObject(TypeSchema typeSchema, Object rootObject, 
                                     JSON5Object rootJSON5Object, SerializationContext context) {
        if (rootObject == null) {
            return null;
        }
        
        try {
            // 기존 deprecated 메소드를 직접 호출하여 안정성 확보
            return serializeTypeElement_deprecated(typeSchema, rootObject);
        } catch (Exception e) {
            throw new SerializationException("Object serialization failed for type: " + 
                typeSchema.getType().getName(), e);
        }
    }
    
    /**
     * 기존 JSON5Serializer의 serializeTypeElement_deprecated 메소드를 복사했습니다.
     * 
     * @param typeSchema 타입 스키마
     * @param rootObject 루트 객체
     * @return 직렬화된 JSON5Object
     */
    private JSON5Object serializeTypeElement_deprecated(TypeSchema typeSchema, final Object rootObject) {
        Class<?> type = typeSchema.getType();
        if(rootObject == null) {
            return null;
        }
        SchemaObjectNode schemaRoot = typeSchema.getSchemaObjectNode();

        HashMap<Integer, Object> parentObjMap = new HashMap<>();
        JSON5Element JSON5Element = new JSON5Object();
        String comment = typeSchema.getComment();
        String commentAfter = typeSchema.getCommentAfter();
        if(comment != null) {
            JSON5Element.setHeaderComment(comment);
        }
        if(commentAfter != null) {
            JSON5Element.setFooterComment(commentAfter);
        }
        JSON5Object root = (JSON5Object) JSON5Element;
        ArrayDeque<ObjectSerializeDequeueItem> objectSerializeDequeueItems = new ArrayDeque<>();
        Iterator<Object> iter = schemaRoot.keySet().iterator();
        SchemaObjectNode schemaNode = schemaRoot;
        ObjectSerializeDequeueItem currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, JSON5Element);
        objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);

        while(iter.hasNext()) {
            Object key = iter.next();
            ISchemaNode node = schemaNode.get(key);
            if(node instanceof SchemaObjectNode) {
                schemaNode = (SchemaObjectNode)node;
                iter = schemaNode.keySet().iterator();
                List<SchemaValueAbs> parentschemaField = schemaNode.getParentSchemaFieldList();
                int nullCount = parentschemaField.size();

                // 부모 필드들의 값을 가져온다.
                for(SchemaValueAbs parentSchemaValueAbs : parentschemaField) {
                    int id = parentSchemaValueAbs.getId();
                    if(parentObjMap.containsKey(id)) {
                        continue;
                    }
                    // 부모 필드의 부모 필드가 없으면 rootObject 에서 값을 가져온다.
                    SchemaField grandschemaField = parentSchemaValueAbs.getParentField();
                    Object parentObj = null;
                    if (grandschemaField == null) {
                        parentObj = parentSchemaValueAbs.getValue(rootObject);
                    }
                    else {
                        Object grandObj = parentObjMap.get(grandschemaField.getId());
                        if(grandObj != null) {
                            parentObj = parentSchemaValueAbs.getValue(grandObj);
                        }
                    }
                    if(parentObj != null) {
                        parentObjMap.put(id, parentObj);
                        nullCount--;
                    }
                }

                if(!schemaNode.isBranchNode() && nullCount > 0) {
                    if(key instanceof String) {
                        ((JSON5Object) JSON5Element).put((String) key,null);
                    } else {
                        assert JSON5Element instanceof JSON5Array;
                        ((JSON5Array) JSON5Element).set((Integer) key,null);
                    }
                    while (iter.hasNext())  {
                        iter.next();
                    }
                } else {
                    if(key instanceof String) {
                        JSON5Object currentObject = ((JSON5Object) JSON5Element);
                        JSON5Element childElement = currentObject.getJSON5Object((String) key);
                        if (childElement == null) {
                                childElement = (schemaNode instanceof SchemaArrayNode) ? new JSON5Array() : new JSON5Object();
                                currentObject.put((String) key, childElement);
                                currentObject.setCommentForKey((String) key, schemaNode.getComment());
                                currentObject.setCommentAfterKey((String) key, schemaNode.getAfterComment());
                                JSON5Element = childElement;
                        }

                    } else {
                        if(!(JSON5Element instanceof JSON5Array)) {
                            throw new JSON5SerializerException("Invalide path. '" + key + "' is not array index." +  "(JSON5Element is not JSON5Array. JSON5Element=" + JSON5Element +  ")");
                        }
                        JSON5Array currentObject = ((JSON5Array) JSON5Element);
                        JSON5Array currentArray = ((JSON5Array) JSON5Element);
                        JSON5Element childElement = (JSON5Element) currentArray.get((Integer) key);
                        if(childElement == null) {

                                childElement = (schemaNode instanceof SchemaArrayNode) ? new JSON5Array() : new JSON5Object();
                                currentObject.set((int) key, childElement);
                                JSON5Element = childElement;

                        }
                    }
                    objectSerializeDequeueItems.add(new ObjectSerializeDequeueItem(iter, schemaNode, JSON5Element));
                }
            }
            else if(node instanceof SchemaFieldNormal || SchemaMethod.isSchemaMethodGetter(node)) {
                SchemaValueAbs schemaValueAbs = (SchemaValueAbs)node;
                Object parent = obtainParentObjects(parentObjMap, schemaValueAbs, rootObject);
                if(parent != null) {
                    Object value = schemaValueAbs.getValue(parent);
                    putValueInJSON5Element(JSON5Element, schemaValueAbs, key, value);
                }
            } else if(node instanceof ISchemaMapValue) {
                SchemaValueAbs schemaMap = (SchemaValueAbs)node;
                Object parent = obtainParentObjects(parentObjMap, schemaMap, rootObject);
                if(parent != null) {
                    Object value = schemaMap.getValue(parent);
                    if(value != null) {
                        @SuppressWarnings("unchecked")
                        JSON5Object json5Object = mapObjectToJSON5Object((Map<String, ?>) value, ((ISchemaMapValue)schemaMap).getElementType());
                        putValueInJSON5Element(JSON5Element, schemaMap, key, json5Object);
                    } else {
                        putValueInJSON5Element(JSON5Element, schemaMap, key, null);
                    }
                }

            }
            else if(node instanceof ISchemaArrayValue) {
                ISchemaArrayValue ISchemaArrayValue = (ISchemaArrayValue)node;
                Object parent = obtainParentObjects(parentObjMap, (SchemaValueAbs) ISchemaArrayValue, rootObject);
                if(parent != null) {
                    Object value = ISchemaArrayValue.getValue(parent);
                    if(value != null) {
                        JSON5Array JSON5Array = collectionObjectToSONArrayKnownSchema((Collection<?>)value, ISchemaArrayValue);
                        putValueInJSON5Element(JSON5Element, ISchemaArrayValue, key, JSON5Array);
                    } else {
                        putValueInJSON5Element(JSON5Element, ISchemaArrayValue, key, null);
                    }
                }
            }
            while(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                ObjectSerializeDequeueItem objectSerializeDequeueItem = objectSerializeDequeueItems.getFirst();
                iter = objectSerializeDequeueItem.keyIterator;
                schemaNode = (SchemaObjectNode) objectSerializeDequeueItem.ISchemaNode;
                JSON5Element = objectSerializeDequeueItem.resultElement;
                if(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                    objectSerializeDequeueItems.removeFirst();
                }
            }
        }
        return root;
    }

    // 기존 JSON5Serializer의 helper 메소드들 복사
    private void putValueInJSON5Element(JSON5Element JSON5Element, ISchemaValue ISchemaValueAbs, Object key, Object value) {
        if(key instanceof String) {
            ((JSON5Object) JSON5Element).put((String) key, value);
            ((JSON5Object) JSON5Element).setCommentForKey((String) key, ISchemaValueAbs.getComment());
            ((JSON5Object) JSON5Element).setCommentAfterKey((String) key, ISchemaValueAbs.getAfterComment());
        }
        else {
            if(!(JSON5Element instanceof JSON5Array)) {
                throw new JSON5SerializerException("Invalide path. '" + key + "' is not array index." +  "(JSON5Element is not JSON5Array. JSON5Element=" + JSON5Element +  ")");
            }
            ((JSON5Array) JSON5Element).set((int)key, value);
            ((JSON5Array) JSON5Element).setCommentForValue((int)key, ISchemaValueAbs.getComment()) ;
            ((JSON5Array) JSON5Element).setCommentAfterValue((int)key, ISchemaValueAbs.getAfterComment());
        }
    }

    private Object obtainParentObjects(Map<Integer, Object> parentsMap, SchemaValueAbs schemaField, Object rootObject) {
        SchemaField parentschemaField = schemaField.getParentField();
        if(parentschemaField == null) {
            return rootObject;
        }
        int parentId = parentschemaField.getId();
        return parentsMap.get(parentId);
    }

    private JSON5Object mapObjectToJSON5Object(Map<String, ?> map, Class<?> valueType) {
        // 새로운 MapSerializer 사용
        MapSerializer mapSerializer = new MapSerializer();
        return mapSerializer.serializeMap(map, valueType);
    }

    private JSON5Array collectionObjectToSONArrayKnownSchema(Collection<?> collection, ISchemaArrayValue ISchemaArrayValue) {
        // 새로운 CollectionSerializer 사용
        CollectionSerializer collectionSerializer = new CollectionSerializer();
        return collectionSerializer.serializeCollectionWithSchema(collection, ISchemaArrayValue);
    }

    // 내부 클래스
    private static class ObjectSerializeDequeueItem {
        Iterator<Object> keyIterator;
        ISchemaNode ISchemaNode;
        JSON5Element resultElement;

        private ObjectSerializeDequeueItem(Iterator<Object> keyIterator, ISchemaNode ISchemaNode, JSON5Element resultElement) {
            this.keyIterator = keyIterator;
            this.ISchemaNode = ISchemaNode;
            this.resultElement = resultElement;
        }
    }
}
