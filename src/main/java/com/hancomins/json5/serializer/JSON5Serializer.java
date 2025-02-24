package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.options.WritingOptions;
import com.hancomins.json5.util.DataConverter;


import java.util.*;

public class JSON5Serializer {

    private JSON5Serializer() {}

    public static boolean serializable(Class<?> clazz) {
        if(TypeSchemaMap.getInstance().hasTypeInfo(clazz)) {
            return true;
        }
        return clazz.getAnnotation(JSON5Type.class) != null;
    }

    public static JSON5Object toJSON5Object(Object obj) {
        Objects.requireNonNull(obj, "obj is null");
        Class<?> clazz = obj.getClass();
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        return serializeTypeElement(typeSchema,obj);
    }

    private static JSON5Object serializeTypeElement(TypeSchema typeSchema, final Object rootObject) {
        Class<?> type = typeSchema.getType();
        /*if(rootObject.getClass() != type) {
            throw new JSON5SerializerException("Type mismatch error. " + type.getName() + "!=" + rootObject.getClass().getName());
        }
        else*/ if(rootObject == null) {
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
                        JSON5Element childElement = currentObject.optJSON5Object((String) key);
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
                        JSON5Element childElement = (JSON5Element) currentArray.opt((Integer) key);
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


    private static void putValueInJSON5Element(JSON5Element JSON5Element, ISchemaValue ISchemaValueAbs, Object key, Object value) {
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

    public static JSON5Object mapToJSON5Object(Map<String, ?> map) {
        return mapObjectToJSON5Object(map, null);
    }

    public static JSON5Array collectionToJSON5Array(Collection<?> collection) {
        return collectionObjectToJSON5Array(collection, null);
    }



    public static <T> List<T> json5ArrayToList(JSON5Array json5Array, Class<T> valueType) {
        return json5ArrayToList(json5Array, valueType, null, false, null);
    }

    public static <T> List<T> json5ArrayToList(JSON5Array json5Array, Class<T> valueType, boolean ignoreError) {
        return json5ArrayToList(json5Array, valueType, null, ignoreError, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> json5ArrayToList(JSON5Array json5Array, Class<T> valueType, WritingOptions writingOptions, boolean ignoreError, T defaultValue) {
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
                if(writingOptions != null && value instanceof JSON5Element) {
                    result.add((T)((JSON5Element) value).toString(writingOptions));
                } else {
                    result.add((T) value.toString());
                }
            } else if(value instanceof JSON5Object && JSON5Serializer.serializable(valueType)) {
                try {
                    value = JSON5Serializer.fromJSON5Object((JSON5Object) value, valueType);
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


    private static JSON5Object mapObjectToJSON5Object(Map<String, ?> map, Class<?> valueType) {
        JSON5Object json5Object = new JSON5Object();
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
                JSON5Array JSON5Array = collectionObjectToJSON5Array((Collection<?>)value, null);
                json5Object.put(key, JSON5Array);
            } else if(value instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                JSON5Object childObject = mapObjectToJSON5Object((Map<String, ?>)value, null);
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
                        JSON5Object childObject = toJSON5Object(value);
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




    private static JSON5Array collectionObjectToJSON5Array(Collection<?> collection, Class<?> valueType) {
        JSON5Array JSON5Array = new JSON5Array();
        Types types = valueType == null ? null : Types.of(valueType);
        for(Object object : collection) {
            if(object instanceof Collection<?>) {
                JSON5Array childArray = collectionObjectToJSON5Array((Collection<?>)object, null);
                JSON5Array.add(childArray);
            } else if(object instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                JSON5Object childObject = mapObjectToJSON5Object((Map<String, ?>)object, null);
                JSON5Array.add(childObject);
            } else if(types == Types.Object) {
                JSON5Object childObject = toJSON5Object(object);
                JSON5Array.add(childObject);
            }
            else {
                JSON5Array.add(object);
            }
        }
        return JSON5Array;

    }



    private static JSON5Array collectionObjectToSONArrayKnownSchema(Collection<?> collection, ISchemaArrayValue ISchemaArrayValue) {
        JSON5Array resultJSON5Array = new JSON5Array();
        JSON5Array JSON5Array = resultJSON5Array;
        Iterator<?> iter = collection.iterator();
        TypeSchema objectValueTypeSchema = ISchemaArrayValue.getObjectValueTypeElement();
        Deque<ArraySerializeDequeueItem> arraySerializeDequeueItems = new ArrayDeque<>();
        ArraySerializeDequeueItem currentArraySerializeDequeueItem = new ArraySerializeDequeueItem(iter, JSON5Array);
        arraySerializeDequeueItems.add(currentArraySerializeDequeueItem);
        boolean isGeneric = ISchemaArrayValue.isGenericTypeValue();
        boolean isAbstractObject = ISchemaArrayValue.getEndpointValueType() == Types.AbstractObject;
        while(iter.hasNext()) {
            Object object = iter.next();
            if(object instanceof Collection<?>) {
                JSON5Array childArray = new JSON5Array();
                JSON5Array.add(childArray);
                JSON5Array = childArray;
                iter = ((Collection<?>)object).iterator();
                currentArraySerializeDequeueItem = new ArraySerializeDequeueItem(iter, JSON5Array);
                arraySerializeDequeueItems.add(currentArraySerializeDequeueItem);
            } else if(objectValueTypeSchema == null) {
                if(isGeneric || isAbstractObject) {
                    object = object == null ? null :  JSON5Serializer.toJSON5Object(object);
                }
                JSON5Array.add(object);
            } else {
                if(object == null)  {
                    JSON5Array.add(null);
                } else {
                    JSON5Object childObject = serializeTypeElement(objectValueTypeSchema, object);
                    JSON5Array.add(childObject);
                }
            }
            while(!iter.hasNext() && !arraySerializeDequeueItems.isEmpty()) {
                ArraySerializeDequeueItem arraySerializeDequeueItem = arraySerializeDequeueItems.getFirst();
                iter = arraySerializeDequeueItem.iterator;
                JSON5Array = arraySerializeDequeueItem.JSON5Array;
                if(!iter.hasNext() && !arraySerializeDequeueItems.isEmpty()) {
                    arraySerializeDequeueItems.removeFirst();
                }
            }
        }
        return resultJSON5Array;

    }


    private static Object obtainParentObjects(Map<Integer, Object> parentsMap, SchemaValueAbs schemaField, Object rootObject) {
        SchemaField parentschemaField = schemaField.getParentField();
        if(parentschemaField == null) {
            return rootObject;
        }
        int parentId = parentschemaField.getId();
        return parentsMap.get(parentId);
    }


    /**
     * JSON5Object 를 Map<String, T> 로 변환한다.
     * @param json5Object 변환할 JSON5Object
     * @param valueType Map 의 value 타입 클래스
     * @return 변환된 Map
     * @param <T> Map 의 value 타입
     */
    @SuppressWarnings({"unchecked", "unused"})
    public static <T> Map<String, T> fromJSON5ObjectToMap(JSON5Object json5Object, Class<T> valueType) {
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
        return (Map<String, T>) fromJSON5ObjectToMap(null, json5Object, valueType,null);

    }

    private static interface OnObtainTypeValue {
        Object obtain(Object target);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static  Map<?, ?> fromJSON5ObjectToMap(Map target, JSON5Object json5Object, Class valueType, OnObtainTypeValue onObtainTypeValue) {
        Types types = Types.of(valueType);
        if(target == null) {
            target = new HashMap<>();
        }

        Map finalTarget = target;
        if(onObtainTypeValue != null) {
            json5Object.keySet().forEach(key -> {
                Object childInJson5Object = json5Object.opt(key);
                if(childInJson5Object == null) {
                    finalTarget.put(key, null);
                    return;
                }
                Object targetChild = onObtainTypeValue.obtain(childInJson5Object);
                if(targetChild == null) {
                    finalTarget.put(key, null);
                    return;
                }
                Types targetChildTypes = Types.of(targetChild.getClass());
                if(childInJson5Object instanceof JSON5Object && !Types.isSingleType(targetChildTypes)) {
                    fromJSON5Object((JSON5Object) childInJson5Object, targetChild);
                }
                finalTarget.put(key, targetChild);

            });
        }
        else if(Types.isSingleType(types)) {
            json5Object.keySet().forEach(key -> {
                Object value = Utils.optFrom(json5Object, key, types);
                finalTarget.put(key, value);
            });
        } else if(types == Types.Object) {
            json5Object.keySet().forEach(key -> {
                JSON5Object child = json5Object.optJSON5Object(key, null);
                if(child != null) {
                    Object  targetChild = fromJSON5Object(child, valueType);
                    finalTarget.put(key, targetChild);
                } else {
                    finalTarget.put(key, null);
                }
            });
        } else if(types == Types.JSON5Object) {
            json5Object.keySet().forEach(key -> {
                JSON5Object child = json5Object.optJSON5Object(key, null);
                if(child != null) finalTarget.put(key, child);
                else finalTarget.put(key, null);
            });
        } else if(types == Types.JSON5Array) {
            json5Object.keySet().forEach(key -> {
                JSON5Array child = json5Object.optJSON5Array(key, null);
                if(child != null) finalTarget.put(key, child);
                else finalTarget.put(key, null);
            });
        } else if(types == Types.JSON5Element) {
            json5Object.keySet().forEach(key -> {
                Object child = json5Object.opt(key);
                if(child instanceof JSON5Element) finalTarget.put(key, child);
                else finalTarget.put(key, null);
            });
        }

        return target;

    }




    @SuppressWarnings("unchecked")
    public static<T> T fromJSON5Object(JSON5Object json5Object, Class<T> clazz) {
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        Object object = typeSchema.newInstance();
        return (T) fromJSON5Object(json5Object, object);
    }


    private static JSON5Element getChildElement(SchemaElementNode schemaElementNode, JSON5Element JSON5Element, Object key) {
        if(key instanceof String) {
            JSON5Object json5Object = (JSON5Object) JSON5Element;
            return schemaElementNode instanceof  SchemaArrayNode ? json5Object.optJSON5Array((String) key) :  json5Object.optJSON5Object((String) key) ;
        } else {
            JSON5Array JSON5Array = (JSON5Array) JSON5Element;
            return schemaElementNode instanceof SchemaArrayNode ?  JSON5Array.optJSON5Array((int) key) :  JSON5Array.optJSON5Object((int) key);
        }

    }

    public static<T> T fromJSON5Object(final JSON5Object json5Object, T targetObject) {
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(targetObject.getClass());
        SchemaObjectNode schemaRoot = typeSchema.getSchemaObjectNode();
        HashMap<Integer, Object> parentObjMap = new HashMap<>();
        JSON5Element JSON5Element = json5Object;
        ArrayDeque<ObjectSerializeDequeueItem> objectSerializeDequeueItems = new ArrayDeque<>();
        Iterator<Object> iter = schemaRoot.keySet().iterator();
        SchemaObjectNode schemaNode = schemaRoot;
        ObjectSerializeDequeueItem currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, json5Object);
        objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);
        while(iter.hasNext()) {
            Object key = iter.next();
            ISchemaNode node = schemaNode.get(key);
            JSON5Element parentsJSON5 = JSON5Element;
            if(node instanceof SchemaElementNode) {
                boolean nullValue = false;
                JSON5Element childElement = getChildElement((SchemaElementNode) node, JSON5Element, key);
                if(key instanceof String) {
                    JSON5Object parentObject = (JSON5Object) JSON5Element;
                    if(childElement == null) {
                        if(parentObject.isNull((String) key)) {
                            nullValue = true;
                        } else {
                            continue;
                        }
                    }
                } else {
                    assert JSON5Element instanceof JSON5Array;
                    JSON5Array parentArray = (JSON5Array) JSON5Element;
                    int index = (Integer)key;
                    if(childElement == null) {
                        if(parentArray.size() <= index || parentArray.isNull(index)) {
                            nullValue = true;
                        } else {
                            continue;
                        }
                    }
                }

                JSON5Element = childElement;
                schemaNode = (SchemaObjectNode)node;
                List<SchemaValueAbs> parentSchemaFieldList = schemaNode.getParentSchemaFieldList();
                for(SchemaValueAbs parentSchemaField : parentSchemaFieldList) {
                    getOrCreateParentObject(parentSchemaField, parentObjMap, targetObject, nullValue, parentsJSON5, json5Object);
                }
                iter = schemaNode.keySet().iterator();
                currentObjectSerializeDequeueItem = new ObjectSerializeDequeueItem(iter, schemaNode, JSON5Element);
                objectSerializeDequeueItems.add(currentObjectSerializeDequeueItem);
            }
            else if(node instanceof SchemaValueAbs && ((SchemaValueAbs)node).types() != Types.Object) {
                SchemaValueAbs schemaField = (SchemaValueAbs) node;
                SchemaValueAbs parentField = schemaField.getParentField();
                if(JSON5Element != null) {
                    Object obj = getOrCreateParentObject(parentField, parentObjMap, targetObject, parentsJSON5, json5Object);
                    setValueTargetFromJSON5Objects(obj, schemaField, JSON5Element, key, json5Object);
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
        return targetObject;
    }

    private static Object getOrCreateParentObject(SchemaValueAbs parentSchemaField, HashMap<Integer, Object> parentObjMap, Object root, JSON5Element JSON5Element, JSON5Object rootJSON5) {
        return getOrCreateParentObject(parentSchemaField, parentObjMap, root, false, JSON5Element, rootJSON5);
    }

    private static Object getOrCreateParentObject(SchemaValueAbs parentSchemaField, HashMap<Integer, Object> parentObjMap, Object root, boolean setNull, JSON5Element json5Element, JSON5Object rootJSON5) {
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


    private static void setValueTargetFromJSON5Objects(Object parents, SchemaValueAbs schemaField, JSON5Element json5, Object key, JSON5Object root) {
        List<SchemaValueAbs> schemaValueAbsList = schemaField.getAllSchemaValueList();
        for(SchemaValueAbs schemaValueAbs : schemaValueAbsList) {
            setValueTargetFromJSON5Object(parents, schemaValueAbs, json5, key,root);
        }
    }

    /**
     * Object의 타입을 읽어서 실제 타입으로 캐스팅한다.
     * @param value Object 타입의 값
     * @param realValue 실제 타입의 값
     * @return
     */
    private static Object dynamicCasting(Object value, Object realValue) {
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
            JSON5Object json5Obj = realValue instanceof JSON5Object ? (JSON5Object) realValue : null;
            if(json5Obj != null) {
                fromJSON5Object(json5Obj, value);
                return value;
            }
        }
        return null;
    }

    private static void setValueTargetFromJSON5Object(Object parents, SchemaValueAbs schemaField, final JSON5Element json5, Object key, JSON5Object root) {
        boolean isArrayType = json5 instanceof JSON5Array;

        /*Object value = isArrayType ? ((JSON5Array) json5).opt((int)key) : ((JSON5Object)json5).opt((String)key);
        //todo null 값에 대하여 어떻게 할 것인지 고민해봐야함.
        if(value == null) {
            boolean isNull = isArrayType ? ((JSON5Array) json5).isNull((int)key) : ((JSON5Object)json5).isNull((String)key);
            if(isNull && !schemaField.isPrimitive()) {
                try {
                    schemaField.setValue(parents, null);
                } catch (Exception ignored) {}
            }
            return;
        }*/
        Types valueType = schemaField.getType();
        if(Types.isSingleType(valueType)) {
            Object valueObj = Utils.optFrom(json5, key, valueType);
            schemaField.setValue(parents, valueObj);
        } else if((Types.AbstractObject == valueType || Types.GenericType == valueType) && schemaField instanceof ObtainTypeValueInvokerGetter) {
            Object val = Utils.optFrom(json5, key, valueType);

            Object obj = makeOnObtainTypeValue((ObtainTypeValueInvokerGetter)schemaField, parents, root).obtain(val) ;//on == null ? null : onObtainTypeValue.obtain(json5 instanceof JSON5Object ? (JSON5Object) json5 : null);
            if(obj == null) {
                obj = schemaField.newInstance();
                obj = dynamicCasting(obj, val);
            }
            schemaField.setValue(parents, obj);
        }
        else if(Types.Collection == valueType) {
            JSON5Array JSON5Array = isArrayType ? ((JSON5Array) json5).optJSON5Array((int)key) : ((JSON5Object)json5).optJSON5Array((String)key);
            if(JSON5Array != null) {
                OnObtainTypeValue onObtainTypeValue = null;
                boolean isGenericOrAbsType = ((ISchemaArrayValue)schemaField).isGenericTypeValue() || ((ISchemaArrayValue)schemaField).isAbstractType();
                if(isGenericOrAbsType) {
                    onObtainTypeValue = makeOnObtainTypeValue((ObtainTypeValueInvokerGetter)schemaField, parents, root);
                }
                json5ArrayToCollectionObject(JSON5Array, (ISchemaArrayValue)schemaField, parents, onObtainTypeValue);
            } else if(isArrayType ? ((JSON5Array) json5).isNull((int)key) : ((JSON5Object)json5).isNull((String)key)) {
                try {
                    schemaField.setValue(parents, null);
                } catch (Exception ignored) {}
            }
        } else if(Types.Object == valueType) {
            JSON5Object json5Obj = isArrayType ? ((JSON5Array) json5).optJSON5Object((int)key) : ((JSON5Object)json5).optJSON5Object((String)key);
            if(json5Obj != null) {
                Object target = schemaField.newInstance();
                fromJSON5Object(json5Obj, target);
                schemaField.setValue(parents, target);
            } else if(isArrayType ? ((JSON5Array) json5).isNull((int)key) : ((JSON5Object)json5).isNull((String)key)) {
                schemaField.setValue(parents, null);
            }
        } else if(Types.Map == valueType) {
            JSON5Object json5Obj = isArrayType ? ((JSON5Array) json5).optJSON5Object((int)key) : ((JSON5Object)json5).optJSON5Object((String)key);
            if(json5Obj != null) {
                Object target = schemaField.newInstance();
                Class<?> type = ((ISchemaMapValue)schemaField).getElementType();
                boolean isGenericOrAbstract = ((ISchemaMapValue)schemaField).isGenericValue() || ((ISchemaMapValue)schemaField).isAbstractType();
                OnObtainTypeValue onObtainTypeValue = null;
                if(isGenericOrAbstract) {
                    onObtainTypeValue = makeOnObtainTypeValue( (ObtainTypeValueInvokerGetter)schemaField, parents, root);
                }
                fromJSON5ObjectToMap((Map<?, ?>) target, json5Obj, type, onObtainTypeValue);
                schemaField.setValue(parents, target);
            } else if(isArrayType ? ((JSON5Array) json5).isNull((int)key) : ((JSON5Object)json5).isNull((String)key)) {
                schemaField.setValue(parents, null);
            }
        } else if(Types.JSON5Object == valueType) {
            JSON5Object value = isArrayType ? ((JSON5Array) json5).optJSON5Object((int)key) : ((JSON5Object)json5).optJSON5Object((String)key);
            schemaField.setValue(parents, value);
        } else if(Types.JSON5Array == valueType) {
            JSON5Array value = isArrayType ? ((JSON5Array) json5).optJSON5Array((int)key) : ((JSON5Object)json5).optJSON5Array((String)key);
            schemaField.setValue(parents, value);
        } else if(Types.JSON5Element == valueType) {
            Object value = isArrayType ? ((JSON5Array) json5).opt((int)key) : ((JSON5Object)json5).opt((String)key);
            if(value instanceof JSON5Element) {
                schemaField.setValue(parents, value);
            } else {
                schemaField.setValue(parents, null);
            }
        }
        else {
            try {
                schemaField.setValue(parents, null);
            } catch (Exception ignored) {}
        }

    }


    private static OnObtainTypeValue makeOnObtainTypeValue(ObtainTypeValueInvokerGetter obtainTypeValueInvokerGetter,Object parents, JSON5Object root) {
        return (json5ObjectOrValue) -> {
            ObtainTypeValueInvoker invoker = obtainTypeValueInvokerGetter.getObtainTypeValueInvoker();
            if(invoker == null ) {
                if(obtainTypeValueInvokerGetter.isIgnoreError()) {
                    return null;
                }
                throw new JSON5SerializerException("To deserialize a generic, abstract or interface type you must have a @ObtainTypeValue annotated method. target=" + obtainTypeValueInvokerGetter.targetPath());
            }
            try {
                Object obj = invoker.obtain(parents, json5ObjectOrValue instanceof JSON5Object ? (JSON5Object) json5ObjectOrValue : new JSON5Object().put("$value", json5ObjectOrValue), root);
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




    private static Object optValueInJSON5Array(JSON5Array JSON5Array, int index, ISchemaArrayValue ISchemaArrayValue) {

        switch (ISchemaArrayValue.getEndpointValueType()) {
            case Byte:
                return JSON5Array.optByte(index);
            case Short:
                return JSON5Array.optShort(index);
            case Integer:
                return JSON5Array.optInt(index);
            case Long:
                return JSON5Array.optLong(index);
            case Float:
                return JSON5Array.optFloat(index);
            case Double:
                return JSON5Array.optDouble(index);
            case Boolean:
                return JSON5Array.optBoolean(index);
            case Character:
                return JSON5Array.optChar(index, '\0');
            case String:
                return JSON5Array.optString(index);
            case JSON5Array:
                return JSON5Array.optJSON5Array(index);
            case JSON5Object:
                return JSON5Array.optJSON5Object(index);
            case Object:
                JSON5Object json5Object = JSON5Array.optJSON5Object(index);
                if(json5Object != null) {
                    Object target = ISchemaArrayValue.getObjectValueTypeElement().newInstance();
                    fromJSON5Object(json5Object, target);
                    return target;
                }
        }
        return null;
    }


    @SuppressWarnings({"rawtypes", "ReassignedVariable", "unchecked"})
    private static void json5ArrayToCollectionObject(JSON5Array JSON5Array, ISchemaArrayValue ISchemaArrayValue, Object parent, OnObtainTypeValue onObtainTypeValue) {
        List<CollectionItems> collectionItems = ISchemaArrayValue.getCollectionItems();
        int collectionItemIndex = 0;
        final int collectionItemSize = collectionItems.size();
        if(collectionItemSize == 0) {
            return;
        }
        CollectionItems collectionItem = collectionItems.get(collectionItemIndex);
        ArrayList<ArraySerializeDequeueItem> arraySerializeDequeueItems = new ArrayList<>();
        ArraySerializeDequeueItem objectItem = new ArraySerializeDequeueItem(JSON5Array,collectionItem.newInstance());
        int end = objectItem.getEndIndex();
        arraySerializeDequeueItems.add(objectItem);

        for(int index = 0; index <= end; ++index) {
            objectItem.setArrayIndex(index);
            if(collectionItem.isGeneric() || collectionItem.isAbstractType()) {
                JSON5Object json5Object = objectItem.JSON5Array.optJSON5Object(index);
                Object object = onObtainTypeValue.obtain(json5Object);
                objectItem.collectionObject.add(object);
            }
            else if (collectionItem.getValueClass() != null) {
                Object value = optValueInJSON5Array(objectItem.JSON5Array, index, ISchemaArrayValue);
                objectItem.collectionObject.add(value);
            } else {
                JSON5Array inArray = objectItem.JSON5Array.optJSON5Array(index);
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
    private static class ArraySerializeDequeueItem {
        Iterator<?> iterator;
        JSON5Array JSON5Array;

        Collection collectionObject;
        int index = 0;
        int arraySize = 0;
        private ArraySerializeDequeueItem(Iterator<?> iterator, JSON5Array JSON5Array) {
            this.iterator = iterator;
            this.JSON5Array = JSON5Array;
        }

        private ArraySerializeDequeueItem(JSON5Array JSON5Array, Collection collection) {
            this.JSON5Array = JSON5Array;
            this.arraySize = JSON5Array.size();
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
        JSON5Element resultElement;

        private ObjectSerializeDequeueItem(Iterator<Object> keyIterator, ISchemaNode ISchemaNode, JSON5Element resultElement) {
            this.keyIterator = keyIterator;
            this.ISchemaNode = ISchemaNode;
            this.resultElement = resultElement;
        }
    }

}
