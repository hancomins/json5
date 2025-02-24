package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import com.hancomins.json5.container.*;
import com.hancomins.json5.options.WritingOptions;
import com.hancomins.json5.util.DataConverter;
import com.hancomins.json5.util.NullValue;

import java.util.*;

public class JSON5Mapper {

    private KeyValueDataContainerFactory keyValueDataContainerFactory = null;
    private ArrayDataContainerFactory arrayDataContainerFactory = null;
    


    private JSON5Mapper() {}

    public static boolean serializable(Class<?> clazz) {
        if(TypeSchemaMap.getInstance().hasTypeInfo(clazz)) {
            return true;
        }
        return clazz.getAnnotation(JSON5Type.class) != null;
    }

    public KeyValueDataContainer toKeyValueDataContainer(Object obj) {
        Objects.requireNonNull(obj, "obj is null");
        Class<?> clazz = obj.getClass();
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        return serializeTypeElement(typeSchema,obj);
    }

    private KeyValueDataContainer serializeTypeElement(TypeSchema typeSchema, final Object rootObject) {
        Class<?> type = typeSchema.getType();
        /*if(rootObject.getClass() != type) {
            throw new JSON5SerializerException("Type mismatch error. " + type.getName() + "!=" + rootObject.getClass().getName());
        }
        else*/ if(rootObject == null) {
            return null;
        }
        SchemaObjectNode schemaRoot = typeSchema.getSchemaObjectNode();

        HashMap<Integer, Object> parentObjMap = new HashMap<>();
        BaseDataContainer json5Element = keyValueDataContainerFactory.create();
        String comment = typeSchema.getComment();
        String commentAfter = typeSchema.getCommentAfter();
        if(comment != null) {
            json5Element.setComment(comment, CommentPosition.HEADER);
        }
        if(commentAfter != null) {
            json5Element.setComment(commentAfter, CommentPosition.FOOTER);
        }
        KeyValueDataContainer root = (KeyValueDataContainer) json5Element;
        ArrayDeque<ObjectSerializeDequeueItem> objectSerializeDequeueItems = new ArrayDeque<>();
        Iterator<Object> iter = schemaRoot.keySet().iterator();
        SchemaObjectNode schemaNode = schemaRoot;
        ObjectSerializeDequeueItem currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, json5Element);
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
                        ((KeyValueDataContainer)json5Element).put((String) key,null);
                    } else {
                        assert json5Element instanceof ArrayDataContainer;
                        ((ArrayDataContainer)json5Element).set((Integer) key,null);
                    }
                    while (iter.hasNext())  {
                        iter.next();
                    }
                } else {
                    if(key instanceof String) {
                        KeyValueDataContainer currentObject = ((KeyValueDataContainer)json5Element);
                        Object value = currentObject.get((String)key);

                        if (!(value instanceof BaseDataContainer)) {
                            BaseDataContainer childElement = (schemaNode instanceof SchemaArrayNode) ? arrayDataContainerFactory.create() : keyValueDataContainerFactory.create();
                            currentObject.put((String) key, childElement);
                            currentObject.setComment((String) key, schemaNode.getComment(), CommentPosition.BEFORE_KEY);
                            currentObject.setComment((String) key, schemaNode.getAfterComment(), CommentPosition.AFTER_KEY);
                            json5Element = childElement;
                        }

                    } else {
                        if(!(json5Element instanceof ArrayDataContainer)) {
                            throw new JSON5SerializerException("Invalide path. '" + key + "' is not array index." +  "(json5Element is not ArrayDataContainer. json5Element=" + json5Element +  ")");
                        }
                        ArrayDataContainer currentObject = ((ArrayDataContainer)json5Element);
                        ArrayDataContainer currentArray = ((ArrayDataContainer)json5Element);
                        BaseDataContainer childElement = (BaseDataContainer) currentArray.get((Integer) key);
                        if(childElement == null) {
                            childElement = (schemaNode instanceof SchemaArrayNode) ? arrayDataContainerFactory.create() : keyValueDataContainerFactory.create();
                            currentObject.set((int) key, childElement);
                            json5Element = childElement;

                        }
                    }
                    objectSerializeDequeueItems.add(new ObjectSerializeDequeueItem(iter, schemaNode, json5Element));
                }
            }
            else if(node instanceof SchemaFieldNormal || SchemaMethod.isSchemaMethodGetter(node)) {
                SchemaValueAbs schemaValueAbs = (SchemaValueAbs)node;
                Object parent = obtainParentObjects(parentObjMap, schemaValueAbs, rootObject);
                if(parent != null) {
                    Object value = schemaValueAbs.getValue(parent);
                    putValueInBaseDataContainer(json5Element, schemaValueAbs, key, value);
                }
            } else if(node instanceof ISchemaMapValue) {
                SchemaValueAbs schemaMap = (SchemaValueAbs)node;
                Object parent = obtainParentObjects(parentObjMap, schemaMap, rootObject);
                if(parent != null) {
                    Object value = schemaMap.getValue(parent);
                    if(value != null) {
                        @SuppressWarnings("unchecked")
                        KeyValueDataContainer json5Object = mapObjectToKeyValueDataContainer((Map<String, ?>) value, ((ISchemaMapValue)schemaMap).getElementType());
                        putValueInBaseDataContainer(json5Element, schemaMap, key, json5Object);
                    } else {
                        putValueInBaseDataContainer(json5Element, schemaMap, key, null);
                    }
                }

            }
            else if(node instanceof ISchemaArrayValue) {
                ISchemaArrayValue ISchemaArrayValue = (ISchemaArrayValue)node;
                Object parent = obtainParentObjects(parentObjMap, (SchemaValueAbs) ISchemaArrayValue, rootObject);
                if(parent != null) {
                    Object value = ISchemaArrayValue.getValue(parent);
                    if(value != null) {
                        ArrayDataContainer json5Array = collectionObjectToJSON5ArrayKnownSchema((Collection<?>)value, ISchemaArrayValue);
                        putValueInBaseDataContainer(json5Element, ISchemaArrayValue, key, json5Array);
                    } else {
                        putValueInBaseDataContainer(json5Element, ISchemaArrayValue, key, null);
                    }
                }
            }
            while(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                ObjectSerializeDequeueItem objectSerializeDequeueItem = objectSerializeDequeueItems.getFirst();
                iter = objectSerializeDequeueItem.keyIterator;
                schemaNode = (SchemaObjectNode) objectSerializeDequeueItem.ISchemaNode;
                json5Element = objectSerializeDequeueItem.resultElement;
                if(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                    objectSerializeDequeueItems.removeFirst();
                }
            }
        }
        return root;
    }


    private void putValueInBaseDataContainer(BaseDataContainer json5Element, ISchemaValue ISchemaValueAbs, Object key, Object value) {
        if(key instanceof String) {
            ((KeyValueDataContainer) json5Element).put((String) key, value);
            ((KeyValueDataContainer) json5Element).setComment((String) key, ISchemaValueAbs.getComment(), CommentPosition.BEFORE_KEY);
            ((KeyValueDataContainer) json5Element).setComment((String) key, ISchemaValueAbs.getAfterComment(), CommentPosition.AFTER_KEY);
        }
        else {
            if(!(json5Element instanceof ArrayDataContainer)) {
                throw new JSON5SerializerException("Invalide path. '" + key + "' is not array index." +  "(json5Element is not ArrayDataContainer. json5Element=" + json5Element +  ")");
            }
            ((ArrayDataContainer)json5Element).set((int)key, value);
            ((ArrayDataContainer)json5Element).setComment((int)key, ISchemaValueAbs.getComment(), CommentPosition.BEFORE_VALUE);
            ((ArrayDataContainer)json5Element).setComment((int)key, ISchemaValueAbs.getAfterComment(), CommentPosition.AFTER_VALUE);
        }
    }

    public KeyValueDataContainer mapToKeyValueDataContainer(Map<String, ?> map) {
        return mapObjectToKeyValueDataContainer(map, null);
    }

    public ArrayDataContainer collectionToArrayDataContainer(Collection<?> collection) {
        return collectionObjectToArrayDataContainer(collection, null);
    }



    public <T> List<T> json5ArrayToList(ArrayDataContainer array, Class<T> valueType) {
        return json5ArrayToList(array, valueType, null, false, null);
    }

    public <T> List<T> json5ArrayToList(ArrayDataContainer array, Class<T> valueType, boolean ignoreError) {
        return json5ArrayToList(array, valueType, null, ignoreError, null);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> json5ArrayToList(ArrayDataContainer json5Array, Class<T> valueType, WritingOptions writingOptions, boolean ignoreError, T defaultValue) {
        Types types = Types.of(valueType);
        if(valueType.isPrimitive()) {
            if(ignoreError) {
                return null;
            }
            throw new JSON5SerializerException("valueType is primitive type. valueType=" + valueType.getName());
        } else if(Collection.class.isAssignableFrom(valueType)) {
            if(ignoreError) {
                return null;
            }
            throw new JSON5SerializerException("valueType is java.util.Collection type. Use a class that wraps your Collection.  valueType=" + valueType.getName());
        }  else if(Map.class.isAssignableFrom(valueType)) {
            if(ignoreError) {
                return null;
            }
            throw new JSON5SerializerException("valueType is java.util.Map type. Use a class that wraps your Map.  valueType=" + valueType.getName());
        } else if(valueType.isArray() && Types.ByteArray != types) {
            if(ignoreError) {
                return null;
            }
            throw new JSON5SerializerException("valueType is Array type. ArrayType cannot be used. valueType=" + valueType.getName());
        }
        ArrayList<T> result = new ArrayList<T>();
        for(int i = 0, n = json5Array.size(); i < n; ++i) {
            Object value = json5Array.get(i);
            if(value == null) {
                result.add(defaultValue);
            }
            else if(Number.class.isAssignableFrom(valueType)) {
                try {
                    Number no = DataConverter.toBoxingNumberOfType(value, (Class<? extends Number>) valueType);
                    result.add((T) no);
                } catch (NumberFormatException e) {
                    if(ignoreError) {
                        result.add(defaultValue);
                        continue;
                    }
                    throw new JSON5SerializerException("valueType is Number type. But value is not Number type. valueType=" + valueType.getName());
                }
            } else if(Boolean.class == valueType) {
                if(value.getClass() == Boolean.class) {
                    result.add((T)value);
                } else {
                    result.add("true".equals(value.toString()) ? (T)Boolean.TRUE : (T)Boolean.FALSE);
                }
            } else if(Character.class == valueType) {
                try {
                    if (value.getClass() == Character.class) {
                        result.add((T) value);
                    } else {
                        result.add((T) (Character) DataConverter.toChar(value));
                    }
                } catch (NumberFormatException e) {
                    if(ignoreError) {
                        result.add(defaultValue);
                        continue;
                    }
                    throw new JSON5SerializerException("valueType is Character type. But value is not Character type. valueType=" + valueType.getName());
                }
            } else if(valueType == String.class) {
                if(writingOptions != null && value instanceof BaseDataContainer) {
                    JSON5Element JSON5Element = toJSON5Element(value);
                    result.add((T)(JSON5Element.toString(  writingOptions)));
                } else {
                    result.add((T) value.toString());
                }
            } else if(value instanceof KeyValueDataContainer && JSON5Mapper.serializable(valueType)) {
                try {
                    value = fromKeyValueDataContainer((KeyValueDataContainer)value, valueType);
                } catch (JSON5Exception e) {
                    if(ignoreError) {
                        result.add(defaultValue);
                        continue;
                    }
                    throw e;
                }
                result.add((T)value);
            }
        }
        return result;
    }


    private KeyValueDataContainer mapObjectToKeyValueDataContainer(Map<String, ?> map, Class<?> valueType) {
        KeyValueDataContainer json5Object = keyValueDataContainerFactory.create();
        Set<? extends Map.Entry<String, ?>> entries = map.entrySet();
        Types types = valueType == null ? null : Types.of(valueType);
        for(Map.Entry<String, ?> entry : entries) {
            Object value = entry.getValue();
            String key = entry.getKey();
            if(value != null && valueType == null) {
                valueType = value.getClass();
                ISchemaValue.assertValueType(valueType, null);
                types = Types.of(valueType);
                //noinspection DataFlowIssue
                if(!(key instanceof String)) {
                    throw new JSON5SerializerException("Map key type is not String. Please use String key.");
                }
            }
            if(value instanceof Collection<?>) {
                ArrayDataContainer json5Array = collectionObjectToArrayDataContainer((Collection<?>)value, null);
                json5Object.put(key, json5Array);
            } else if(value instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                KeyValueDataContainer childObject = mapObjectToKeyValueDataContainer((Map<String, ?>)value, null);
                json5Object.put(key, childObject);
            } else if(types == Types.Object) {
                if(value == null) {
                    json5Object.put(key, null);
                }
                else {
                    Types type = Types.of(value.getClass());
                    if(Types.isSingleType(type)) {
                        json5Object.put(key, value);
                    } else {
                        KeyValueDataContainer childObject = toKeyValueDataContainer(value);
                        json5Object.put(key, childObject);
                    }
                }
            }
            else {
                json5Object.put(entry.getKey(), value);
            }
        }
        return json5Object;
    }




    private ArrayDataContainer collectionObjectToArrayDataContainer(Collection<?> collection, Class<?> valueType) {
        ArrayDataContainer json5Array = arrayDataContainerFactory.create();
        Types types = valueType == null ? null : Types.of(valueType);
        for(Object object : collection) {
            if(object instanceof Collection<?>) {
                ArrayDataContainer childArray = collectionObjectToArrayDataContainer((Collection<?>)object, null);
                json5Array.add(childArray);
            } else if(object instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                KeyValueDataContainer childObject = mapObjectToKeyValueDataContainer((Map<String, ?>)object, null);
                json5Array.add(childObject);
            } else if(types == Types.Object) {
                KeyValueDataContainer childObject = toKeyValueDataContainer(object);
                json5Array.add(childObject);
            }
            else {
                json5Array.add(object);
            }
        }
        return json5Array;

    }



    private ArrayDataContainer collectionObjectToJSON5ArrayKnownSchema(Collection<?> collection, ISchemaArrayValue ISchemaArrayValue) {
        ArrayDataContainer resultJSON5Array  = arrayDataContainerFactory.create();
        ArrayDataContainer json5Array = resultJSON5Array;
        Iterator<?> iter = collection.iterator();
        TypeSchema objectValueTypeSchema = ISchemaArrayValue.getObjectValueTypeElement();
        Deque<ArraySerializeDequeueItem> arraySerializeDequeueItems = new ArrayDeque<>();
        ArraySerializeDequeueItem currentArraySerializeDequeueItem = new ArraySerializeDequeueItem(iter, json5Array);
        arraySerializeDequeueItems.add(currentArraySerializeDequeueItem);
        boolean isGeneric = ISchemaArrayValue.isGenericTypeValue();
        boolean isAbstractObject = ISchemaArrayValue.getEndpointValueType() == Types.AbstractObject;
        while(iter.hasNext()) {
            Object object = iter.next();
            if(object instanceof Collection<?>) {
                ArrayDataContainer childArray = arrayDataContainerFactory.create();
                json5Array.add(childArray);
                json5Array = childArray;
                iter = ((Collection<?>)object).iterator();
                currentArraySerializeDequeueItem = new ArraySerializeDequeueItem(iter, json5Array);
                arraySerializeDequeueItems.add(currentArraySerializeDequeueItem);
            } else if(objectValueTypeSchema == null) {
                if(isGeneric || isAbstractObject) {
                    object = object == null ? null :  toKeyValueDataContainer(object);
                }
                json5Array.add(object);
            } else {
                if(object == null)  {
                    json5Array.add(null);
                } else {
                    KeyValueDataContainer childObject = serializeTypeElement(objectValueTypeSchema, object);
                    json5Array.add(childObject);
                }
            }
            while(!iter.hasNext() && !arraySerializeDequeueItems.isEmpty()) {
                ArraySerializeDequeueItem arraySerializeDequeueItem = arraySerializeDequeueItems.getFirst();
                iter = arraySerializeDequeueItem.iterator;
                json5Array = arraySerializeDequeueItem.json5Array;
                if(!iter.hasNext() && !arraySerializeDequeueItems.isEmpty()) {
                    arraySerializeDequeueItems.removeFirst();
                }
            }
        }
        return resultJSON5Array;

    }


    private Object obtainParentObjects(Map<Integer, Object> parentsMap, SchemaValueAbs schemaField, Object rootObject) {
        SchemaField parentschemaField = schemaField.getParentField();
        if(parentschemaField == null) {
            return rootObject;
        }
        int parentId = parentschemaField.getId();
        return parentsMap.get(parentId);
    }


    /**
     * KeyValueDataContainer 를 Map<String, T> 로 변환한다.
     * @param json5Object 변환할 KeyValueDataContainer
     * @param valueType Map 의 value 타입 클래스
     * @return 변환된 Map
     * @param <T> Map 의 value 타입
     */
    @SuppressWarnings({"unchecked", "unused"})
    public <T> Map<String, T> fromKeyValueDataContainerToMap(KeyValueDataContainer json5Object, Class<T> valueType) {
        Types types = Types.of(valueType);
        if(valueType.isPrimitive()) {
            throw new JSON5SerializerException("valueType is primitive type. valueType=" + valueType.getName());
        } else if(Collection.class.isAssignableFrom(valueType)) {
            throw new JSON5SerializerException("valueType is java.util.Collection type. Use a class that wraps your Collection.  valueType=" + valueType.getName());
        }  else if(Collection.class.isAssignableFrom(valueType)) {
            throw new JSON5SerializerException("valueType is java.util.Map type. Use a class that wraps your Map.  valueType=" + valueType.getName());
        } else if(valueType.isArray() && Types.ByteArray != types) {
            throw new JSON5SerializerException("valueType is Array type. ArrayType cannot be used. valueType=" + valueType.getName());
        }
        return (Map<String, T>) fromKeyValueDataContainerToMap(null, json5Object, valueType,null);

    }

    private interface OnObtainTypeValue {
        Object obtain(Object target);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private  Map<?, ?> fromKeyValueDataContainerToMap(Map target, KeyValueDataContainer json5Object, Class valueType, OnObtainTypeValue onObtainTypeValue) {
        Types types = Types.of(valueType);
        if(target == null) {
            target = new HashMap<>();
        }

        Map finalTarget = target;
        if(onObtainTypeValue != null) {
            json5Object.keySet().forEach(key -> {
                Object childInJSON5Object = json5Object.get(key);
                if(childInJSON5Object == null) {
                    finalTarget.put(key, null);
                    return;
                }
                Object targetChild = onObtainTypeValue.obtain(childInJSON5Object);
                if(targetChild == null) {
                    finalTarget.put(key, null);
                    return;
                }
                Types targetChildTypes = Types.of(targetChild.getClass());
                if(childInJSON5Object instanceof KeyValueDataContainer && !Types.isSingleType(targetChildTypes)) {
                    fromKeyValueDataContainer((KeyValueDataContainer) childInJSON5Object, targetChild);
                }
                finalTarget.put(key, targetChild);

            });
        }
        else if(Types.isSingleType(types)) {
            json5Object.keySet().forEach(key -> {
                Object value = optFrom(json5Object, key, types);
                finalTarget.put(key, value);
            });
        } else if(types == Types.Object) {
            json5Object.keySet().forEach(key -> {
                Object child = json5Object.get(key);
                if(child instanceof KeyValueDataContainer) {
                    Object targetChild = fromKeyValueDataContainer((KeyValueDataContainer)child, valueType);
                    finalTarget.put(key, targetChild);
                } else {
                    finalTarget.put(key, null);
                }
            });
        } else if(types == Types.JSON5Object) {
            json5Object.keySet().forEach(key -> {
                Object child = json5Object.get(key);
                if(child instanceof KeyValueDataContainer) finalTarget.put(key, child);
                else finalTarget.put(key, null);
            });
        } else if(types == Types.JSON5Array) {
            json5Object.keySet().forEach(key -> {
                Object child = json5Object.get(key);
                if(child instanceof ArrayDataContainer) finalTarget.put(key, child);
                else finalTarget.put(key, null);
            });
        } else if(types == Types.JSON5Element) {
            json5Object.keySet().forEach(key -> {
                Object child = json5Object.get(key);
                if(child instanceof BaseDataContainer) finalTarget.put(key, child);
                else finalTarget.put(key, null);
            });
        }
        return target;

    }




    @SuppressWarnings("unchecked")
    public<T> T fromKeyValueDataContainer(KeyValueDataContainer json5Object, Class<T> clazz) {
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        Object object = typeSchema.newInstance();
        return (T) fromKeyValueDataContainer(json5Object, object);
    }


    private BaseDataContainer getChildElement(SchemaElementNode schemaElementNode,BaseDataContainer json5Element, Object key) {
        if(key instanceof String) {
            KeyValueDataContainer json5Object = (KeyValueDataContainer)json5Element;
            Object value = json5Object.get((String) key);
            if(schemaElementNode instanceof  SchemaArrayNode && value instanceof ArrayDataContainer) {
                return (ArrayDataContainer) value;
            } else if(value instanceof KeyValueDataContainer) {
                return (KeyValueDataContainer) value;
            }
            return null;
        } else {
            ArrayDataContainer json5Array = (ArrayDataContainer) json5Element;
            Object value = json5Array.get((int) key);
            if(schemaElementNode instanceof SchemaArrayNode &&  value instanceof ArrayDataContainer) {
                return (ArrayDataContainer) value;
            } else if(value instanceof KeyValueDataContainer) {
                return (KeyValueDataContainer) value;
            }
            return null;
        }

    }

    public<T> T fromKeyValueDataContainer(final KeyValueDataContainer json5Object, T targetObject) {
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(targetObject.getClass());
        SchemaObjectNode schemaRoot = typeSchema.getSchemaObjectNode();
        HashMap<Integer, Object> parentObjMap = new HashMap<>();
        BaseDataContainer json5Element = json5Object;
        ArrayDeque<ObjectSerializeDequeueItem> objectSerializeDequeueItems = new ArrayDeque<>();
        Iterator<Object> iter = schemaRoot.keySet().iterator();
        SchemaObjectNode schemaNode = schemaRoot;
        ObjectSerializeDequeueItem currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, json5Object);
        objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);
        while(iter.hasNext()) {
            Object key = iter.next();
            ISchemaNode node = schemaNode.get(key);
            BaseDataContainer parentsJSON5 = json5Element;
            if(node instanceof SchemaElementNode) {
                boolean nullValue = false;
                BaseDataContainer childElement = getChildElement((SchemaElementNode) node, json5Element, key);
                if(key instanceof String) {
                    KeyValueDataContainer parentObject = (KeyValueDataContainer) json5Element;
                    if(childElement == null && parentObject != null) {
                        Object v = parentObject.get((String) key);
                        if(v == null || v == NullValue.Instance) {
                            nullValue = true;
                        } else {
                            continue;
                        }
                    }
                } else {
                    assert json5Element instanceof ArrayDataContainer;
                    ArrayDataContainer parentArray = (ArrayDataContainer)json5Element;
                    int index = (Integer)key;
                    if(childElement == null) {
                        if(parentArray.size() <= index) {
                            nullValue = true;
                        } else {
                            Object v = parentArray.get(index);
                            if(v == null || v == NullValue.Instance) {
                                nullValue = true;
                            } else {
                                continue;
                            }
                        }
                    }
                }

                json5Element = childElement;
                schemaNode = (SchemaObjectNode)node;
                List<SchemaValueAbs> parentSchemaFieldList = schemaNode.getParentSchemaFieldList();
                for(SchemaValueAbs parentSchemaField : parentSchemaFieldList) {
                    getOrCreateParentObject(parentSchemaField, parentObjMap, targetObject, nullValue, parentsJSON5, json5Object);
                }
                iter = schemaNode.keySet().iterator();
                currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, json5Element);
                objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);
            }
            else if(node instanceof SchemaValueAbs && ((SchemaValueAbs)node).types() != Types.Object) {
                SchemaValueAbs schemaField = (SchemaValueAbs) node;
                SchemaValueAbs parentField = schemaField.getParentField();
                if(json5Element != null) {
                    Object obj = getOrCreateParentObject(parentField, parentObjMap, targetObject, parentsJSON5, json5Object);
                    setValueTargetFromKeyValueDataContainers(obj, schemaField, json5Element, key, json5Object);
                }
            }
            while(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                ObjectSerializeDequeueItem objectSerializeDequeueItem = objectSerializeDequeueItems.getFirst();
                iter = objectSerializeDequeueItem.keyIterator;
                schemaNode = (SchemaObjectNode) objectSerializeDequeueItem.ISchemaNode;
                json5Element = objectSerializeDequeueItem.resultElement;
                if(!iter.hasNext() && !objectSerializeDequeueItems.isEmpty()) {
                    objectSerializeDequeueItems.removeFirst();
                }
            }
        }
        return targetObject;
    }

    private Object getOrCreateParentObject(SchemaValueAbs parentSchemaField, HashMap<Integer, Object> parentObjMap, Object root, BaseDataContainer json5Element, KeyValueDataContainer rootJSON5) {
        return getOrCreateParentObject(parentSchemaField, parentObjMap, root, false, json5Element, rootJSON5);
    }

    private Object getOrCreateParentObject(SchemaValueAbs parentSchemaField, HashMap<Integer, Object> parentObjMap, Object root, boolean setNull, BaseDataContainer json5Element, KeyValueDataContainer rootJSON5) {
        if(parentSchemaField == null) return root;

        int id = parentSchemaField.getId();
        Object parent = parentObjMap.get(id);
        if (parent != null) {
            return parent;
        }
        ArrayList<SchemaValueAbs>  pedigreeList = new ArrayList<>();
        while(parentSchemaField != null) {
            pedigreeList.add(parentSchemaField);
            parentSchemaField = parentSchemaField.getParentField();
        }
        Collections.reverse(pedigreeList);
        parent = root;
        SchemaValueAbs last = pedigreeList.get(pedigreeList.size() - 1);
        for(SchemaValueAbs schemaField : pedigreeList) {
           int parentId = schemaField.getId();
           Object child = parentObjMap.get(parentId);
           if(setNull && child == null && schemaField == last) {
                schemaField.setValue(parent, null);
           }
           else if(!setNull && child == null) {
                if(schemaField instanceof ObtainTypeValueInvokerGetter) {
                    // TODO 앞으로 제네릭 또는 interface 나 추상 클래스로만 사용 가능하도록 변경할 것.
                    ObtainTypeValueInvoker obtainTypeValueInvoker = ((ObtainTypeValueInvokerGetter)schemaField).getObtainTypeValueInvoker();
                    if(obtainTypeValueInvoker != null) {
                        OnObtainTypeValue onObtainTypeValue = makeOnObtainTypeValue((ObtainTypeValueInvokerGetter)schemaField, parent, rootJSON5);
                        child = onObtainTypeValue.obtain(json5Element);
                    }
                }
                if(child == null) {
                   child = schemaField.newInstance();
                }
                parentObjMap.put(parentId, child);
                schemaField.setValue(parent, child);
           }
           parent = child;
        }
        return parent;

    }


    private void setValueTargetFromKeyValueDataContainers(Object parents, SchemaValueAbs schemaField, BaseDataContainer json5, Object key, KeyValueDataContainer root) {
        List<SchemaValueAbs> schemaValueAbsList = schemaField.getAllSchemaValueList();
        for(SchemaValueAbs schemaValueAbs : schemaValueAbsList) {
            setValueTargetFromKeyValueDataContainer(parents, schemaValueAbs, json5, key,root);
        }
    }

    /**
     * Object의 타입을 읽어서 실제 타입으로 캐스팅한다.
     * @param value Object 타입의 값
     * @param realValue 실제 타입의 값
     * @return
     */
    private Object dynamicCasting(Object value, Object realValue) {
        if(value == null) return null;
        Class<?> valueClas = value.getClass();
        Types valueType = Types.of(value.getClass());
        if(valueClas.isEnum()) {
            try {
                //noinspection unchecked
                return Enum.valueOf((Class<? extends Enum>) valueClas,realValue.toString());
            } catch (Exception e) {
                return null;
            }
        }
        if(Types.isSingleType(valueType) || Types.isJSON5Type(valueType)) {
            return DataConverter.convertValue(valueClas, realValue);
        } else if(Types.Object == valueType) {
            KeyValueDataContainer json5Obj = realValue instanceof KeyValueDataContainer ? (KeyValueDataContainer) realValue : null;
            if(json5Obj != null) {
                fromKeyValueDataContainer(json5Obj, value);
                return value;
            }
        }
        return null;
    }

    private void setValueTargetFromKeyValueDataContainer(Object parents, SchemaValueAbs schemaField,final BaseDataContainer json5, Object key, KeyValueDataContainer root) {
        boolean isArrayType = json5 instanceof ArrayDataContainer;

        /*Object value = isArrayType ? ((ArrayDataContainer) json5).opt((int)key) : ((KeyValueDataContainer)json5).opt((String)key);
        //todo null 값에 대하여 어떻게 할 것인지 고민해봐야함.
        if(value == null) {
            boolean isNull = isArrayType ? ((ArrayDataContainer) json5).isNull((int)key) : ((KeyValueDataContainer)json5).isNull((String)key);
            if(isNull && !schemaField.isPrimitive()) {
                try {
                    schemaField.setValue(parents, null);
                } catch (Exception ignored) {}
            }
            return;
        }*/
        Types valueType = schemaField.getType();
        if(Types.isSingleType(valueType)) {
            Object valueObj = optFrom(json5, key, valueType);
            schemaField.setValue(parents, valueObj);
        } else if((Types.AbstractObject == valueType || Types.GenericType == valueType) && schemaField instanceof ObtainTypeValueInvokerGetter) {
            Object val = optFrom(json5, key, valueType);

            Object obj = makeOnObtainTypeValue((ObtainTypeValueInvokerGetter)schemaField, parents, root).obtain(val) ;//on == null ? null : onObtainTypeValue.obtain(json5 instanceof KeyValueDataContainer ? (KeyValueDataContainer) json5 : null);
            if(obj == null) {
                obj = schemaField.newInstance();
                obj = dynamicCasting(obj, val);
            }
            schemaField.setValue(parents, obj);
        }
        else if(Types.Collection == valueType) {
            Object value;
            if(isArrayType) {
                value = ((ArrayDataContainer) json5).get((int)key);
            } else {
                value = ((KeyValueDataContainer)json5).get((String)key);
            }
            if(value instanceof ArrayDataContainer) {
                OnObtainTypeValue onObtainTypeValue = null;
                boolean isGenericOrAbsType = ((ISchemaArrayValue)schemaField).isGenericTypeValue() || ((ISchemaArrayValue)schemaField).isAbstractType();
                if(isGenericOrAbsType) {
                    onObtainTypeValue = makeOnObtainTypeValue((ObtainTypeValueInvokerGetter)schemaField, parents, root);
                }
                json5ArrayToCollectionObject((ArrayDataContainer)value, (ISchemaArrayValue)schemaField, parents, onObtainTypeValue);
            } else if(isNull(json5,key)) {
                try {
                    schemaField.setValue(parents, null);
                } catch (Exception ignored) {}
            }
        } else if(Types.Object == valueType) {
            Object value;
            if(isArrayType) {
                value = ((ArrayDataContainer) json5).get((int)key);
            } else {
                value = ((KeyValueDataContainer)json5).get((String)key);
            }

            if(value instanceof KeyValueDataContainer) {
                Object target = schemaField.newInstance();
                fromKeyValueDataContainer((KeyValueDataContainer) value, target);
                schemaField.setValue(parents, target);
            } else if(isNull(json5,key)) {
                schemaField.setValue(parents, null);
            }
        } else if(Types.Map == valueType) {
            Object value;
            if(isArrayType) {
                value = ((ArrayDataContainer) json5).get((int)key);
            } else {
                value = ((KeyValueDataContainer)json5).get((String)key);
            }

            if(value instanceof KeyValueDataContainer) {
                Object target = schemaField.newInstance();
                Class<?> type = ((ISchemaMapValue)schemaField).getElementType();
                boolean isGenericOrAbstract = ((ISchemaMapValue)schemaField).isGenericValue() || ((ISchemaMapValue)schemaField).isAbstractType();
                OnObtainTypeValue onObtainTypeValue = null;
                if(isGenericOrAbstract) {
                    onObtainTypeValue = makeOnObtainTypeValue( (ObtainTypeValueInvokerGetter)schemaField, parents, root);
                }
                fromKeyValueDataContainerToMap((Map<?, ?>) target, (KeyValueDataContainer)value, type, onObtainTypeValue);
                schemaField.setValue(parents, target);
            } else if(isNull(json5,key)) {
                schemaField.setValue(parents, null);
            }
        } else if(Types.JSON5Array == valueType || Types.JSON5Element == valueType || Types.JSON5Object == valueType) {
            Object value = optValue(json5, key);
            JSON5Element JSON5Element = toJSON5Element(value);
            schemaField.setValue(parents, JSON5Element);
        }
        else {
            try {
                schemaField.setValue(parents, null);
            } catch (Exception ignored) {}
        }
    }

    private static JSON5Element toJSON5Element(Object object) {
        if(object instanceof KeyValueDataContainer) {
            return toJSON5Object((KeyValueDataContainer)object);
        } else if(object instanceof ArrayDataContainer) {
            return toJSON5Array((ArrayDataContainer)object);
        }
        return null;
    }

    private static JSON5Array toJSON5Array(ArrayDataContainer arrayDataContainer) {
        JSON5Array JSON5Array = new JSON5Array();
        for(int i = 0, n = arrayDataContainer.size(); i < n; ++i) {
            Object value = arrayDataContainer.get(i);
            if(value instanceof KeyValueDataContainer) {
                JSON5Array.add(toJSON5Object((KeyValueDataContainer)value));
            } else if(value instanceof ArrayDataContainer) {
                JSON5Array.add(toJSON5Array((ArrayDataContainer)value));
            } else {
                JSON5Array.add(value);
            }
        }
        return JSON5Array;
    }

    private static JSON5Object toJSON5Object(KeyValueDataContainer keyValueDataContainer) {
        JSON5Object json5Object = new JSON5Object();
        for(String key : keyValueDataContainer.keySet()) {
            Object value = keyValueDataContainer.get(key);
            if(value instanceof KeyValueDataContainer) {
                json5Object.put(key, toJSON5Object((KeyValueDataContainer)value));
            } else if(value instanceof ArrayDataContainer) {
                json5Object.put(key, toJSON5Array((ArrayDataContainer)value));
            } else {
                json5Object.put(key, value);
            }
        }
        return json5Object;
    }

    private static Object optValue(BaseDataContainer json5, Object key) {
        Object value = null;
        if(key instanceof String && json5 instanceof KeyValueDataContainer) {
            value = ((KeyValueDataContainer)json5).get((String)key);
        } else if(json5 instanceof ArrayDataContainer) {
            value = ((ArrayDataContainer)json5).get((int)key);
        }
        return value;
    }

    private static ArrayDataContainer optArrayDataContainer(BaseDataContainer json5, Object key) {
        Object value = optValue(json5, key);
        return value instanceof ArrayDataContainer ? (ArrayDataContainer)value : null;
    }

    private static KeyValueDataContainer optKeyValueDataContainer(BaseDataContainer json5, Object key) {
        Object value = optValue(json5, key);
        return value instanceof KeyValueDataContainer ? (KeyValueDataContainer)value : null;
    }


    private OnObtainTypeValue makeOnObtainTypeValue(ObtainTypeValueInvokerGetter obtainTypeValueInvokerGetter,Object parents, KeyValueDataContainer root) {
        return (json5ObjectOrValue) -> {
            ObtainTypeValueInvoker invoker = obtainTypeValueInvokerGetter.getObtainTypeValueInvoker();
            if(invoker == null ) {
                if(obtainTypeValueInvokerGetter.isIgnoreError()) {
                    return null;
                }
                throw new JSON5SerializerException("To deserialize a generic, abstract or interface type you must have a @ObtainTypeValue annotated method. target=" + obtainTypeValueInvokerGetter.targetPath());
            }
            try {
                Object json5;
                if(json5ObjectOrValue instanceof KeyValueDataContainer) {
                    json5 = json5ObjectOrValue;
                } else  {
                    json5 =  keyValueDataContainerFactory.create();
                    ((KeyValueDataContainer)json5).put("$value",  root);
                }


                Object obj = invoker.obtain(parents, toJSON5Object((KeyValueDataContainer)json5), toJSON5Object(root));
                if (obj != null && invoker.isDeserializeAfter()) {
                    obj = dynamicCasting(obj, json5ObjectOrValue);
                }
                return obj;
            } catch (RuntimeException e) {
                if(obtainTypeValueInvokerGetter.isIgnoreError()) {
                    return null;
                }
                throw e;
            }
        };
    }



    private static boolean isNull(BaseDataContainer json5Element, Object key) {
        Object value;
        if(key instanceof String && json5Element instanceof KeyValueDataContainer) {
            value = ((KeyValueDataContainer)json5Element).get((String) key);
        } else if(json5Element instanceof ArrayDataContainer) {
            value = ((ArrayDataContainer)json5Element).get((int) key);
        } else {
            return false;
        }
        return value == null || value == NullValue.Instance;

    }



    private ArrayDataContainer optValueInArrayDataContainer(ArrayDataContainer json5Array, int index) {
        Object value = json5Array.get(index);
        return value instanceof ArrayDataContainer ? (ArrayDataContainer)value : null;
    }


    private Object optValueInArrayDataContainer(ArrayDataContainer json5Array, int index, ISchemaArrayValue ISchemaArrayValue) {
        Types types = ISchemaArrayValue.getEndpointValueType();
        if(types == Types.Object) {
            Object value = json5Array.get(index);
            if(value instanceof KeyValueDataContainer) {
                Object target = ISchemaArrayValue.getObjectValueTypeElement().newInstance();
                fromKeyValueDataContainer((KeyValueDataContainer) value, target);
                return target;
            }
        }
        return optFrom(json5Array, index, types);
    }


    @SuppressWarnings({"rawtypes", "ReassignedVariable", "unchecked"})
    private void json5ArrayToCollectionObject(ArrayDataContainer json5Array, ISchemaArrayValue ISchemaArrayValue, Object parent, OnObtainTypeValue onObtainTypeValue) {
        List<CollectionItems> collectionItems = ISchemaArrayValue.getCollectionItems();
        int collectionItemIndex = 0;
        final int collectionItemSize = collectionItems.size();
        if(collectionItemSize == 0) {
            return;
        }
        CollectionItems collectionItem = collectionItems.get(collectionItemIndex);
        ArrayList<ArraySerializeDequeueItem> arraySerializeDequeueItems = new ArrayList<>();
        ArraySerializeDequeueItem objectItem = new ArraySerializeDequeueItem(json5Array,collectionItem.newInstance());
        int end = objectItem.getEndIndex();
        arraySerializeDequeueItems.add(objectItem);

        for(int index = 0; index <= end; ++index) {
            objectItem.setArrayIndex(index);
            if(collectionItem.isGeneric() || collectionItem.isAbstractType()) {
                KeyValueDataContainer json5Object =  optKeyValueDataContainer(objectItem.json5Array,index);
                Object object = onObtainTypeValue.obtain(json5Object);
                objectItem.collectionObject.add(object);
            }
            else if (collectionItem.getValueClass() != null) {
                Object value = optValueInArrayDataContainer(objectItem.json5Array, index, ISchemaArrayValue);
                objectItem.collectionObject.add(value);
            } else {
                ArrayDataContainer inArray = optValueInArrayDataContainer(objectItem.json5Array, index);
                if (inArray == null) {
                    objectItem.collectionObject.add(null);
                } else {
                    collectionItem = collectionItems.get(++collectionItemIndex);
                    Collection newCollection = collectionItem.newInstance();
                    objectItem.collectionObject.add(newCollection);
                    ArraySerializeDequeueItem newArraySerializeDequeueItem = new ArraySerializeDequeueItem(inArray, newCollection);
                    arraySerializeDequeueItems.add(newArraySerializeDequeueItem);
                    index = -1;
                    end = newArraySerializeDequeueItem.getEndIndex();
                    objectItem = newArraySerializeDequeueItem;
                }
            }
            while (index == end) {
                arraySerializeDequeueItems.remove(arraySerializeDequeueItems.size() - 1);
                if (arraySerializeDequeueItems.isEmpty()) {
                    break;
                }
                objectItem = arraySerializeDequeueItems.get(arraySerializeDequeueItems.size() - 1);
                index = objectItem.index;
                end = objectItem.arraySize - 1;
                collectionItem = collectionItems.get(--collectionItemIndex);
            }
        }
        ISchemaArrayValue.setValue(parent, objectItem.collectionObject);
    }





    @SuppressWarnings("rawtypes")
    private class ArraySerializeDequeueItem {
        Iterator<?> iterator;
        ArrayDataContainer json5Array;

        Collection collectionObject;
        int index = 0;
        int arraySize = 0;
        private ArraySerializeDequeueItem(Iterator<?> iterator,ArrayDataContainer json5Array) {
            this.iterator = iterator;
            this.json5Array = json5Array;
        }

        private ArraySerializeDequeueItem(ArrayDataContainer json5Array, Collection collection) {
            this.json5Array = json5Array;
            this.arraySize = json5Array.size();
            this.collectionObject = collection;
        }

        private int getEndIndex() {
            return arraySize - 1;
        }



        /**
         * 역직렬화에서 사용됨.
         *
         */
        private void setArrayIndex(int index) {
            this.index = index;
        }


    }

    private static class ObjectSerializeDequeueItem {
        Iterator<Object> keyIterator;
        ISchemaNode ISchemaNode;
        BaseDataContainer resultElement;

        private ObjectSerializeDequeueItem(Iterator<Object> keyIterator, ISchemaNode ISchemaNode, BaseDataContainer resultElement) {
            this.keyIterator = keyIterator;
            this.ISchemaNode = ISchemaNode;
            this.resultElement = resultElement;
        }
    }



    static Object optFrom(BaseDataContainer baseDataContainer, Object key, Types valueType) {

        Object value = optValue(baseDataContainer, key);
        if(value == null) {
            return null;
        }


        if(Types.Boolean == valueType) {
            return DataConverter.toBoolean(value);
        } else if(Types.Byte == valueType) {
            return DataConverter.toByte(value);
        } else if(Types.Character == valueType) {
            return DataConverter.toChar(value);
        } else if(Types.Long == valueType) {
            return DataConverter.toLong(value);
        } else if(Types.Short == valueType) {
            return DataConverter.toShort(value);
        } else if(Types.Integer == valueType) {
            return DataConverter.toInteger(value);
        } else if(Types.Float == valueType) {
            return DataConverter.toFloat(value);
        } else if(Types.Double == valueType) {
            return DataConverter.toDouble(value);
        } else if(Types.String == valueType) {
            return DataConverter.toString(value);
        }  else if(Types.ByteArray == valueType) {
            return DataConverter.toByteArray(value);
        } else {
            return value;
        }
    }


}
