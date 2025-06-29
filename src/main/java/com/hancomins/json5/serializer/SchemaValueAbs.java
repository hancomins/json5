package com.hancomins.json5.serializer;


import com.hancomins.json5.JSON5Object;

import java.lang.reflect.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

abstract class SchemaValueAbs implements ISchemaNode, ISchemaValue {

    private static final AtomicInteger LAST_ID = new AtomicInteger(1);

    private final int id = LAST_ID.getAndIncrement();

    final TypeSchema parentsTypeSchema;
    final TypeSchema objectTypeSchema;

    final String path;
    private Types type;

    private final boolean isPrimitive;
    final boolean isEnum;

    //private final boolean isMapField;

    private SchemaValueAbs parentFieldRack;
    final Class<?> valueTypeClass;

    private final ArrayList<SchemaValueAbs> allSchemaValueAbsList = new ArrayList<>();


    static SchemaValueAbs of(TypeSchema typeSchema, Field field) {
        int modifiers = field.getModifiers();
        JSON5Value json5Value = field.getAnnotation(JSON5Value.class);
        // 0.9.29 /////////
        if(Modifier.isFinal(modifiers)) {
            if(json5Value == null) {
                return null;
            }
            throw new JSON5SerializerException("@JSON5Value field cannot be final. (path: " + typeSchema.getType().getName() + "." + field.getName() + ")");
        }
        // 0.9.29 /////////
        String key = field.getName();
        if(json5Value != null) {
            if(json5Value.ignore()) return null;
            key = json5Value.key();
            if (key == null || key.isEmpty()) key = json5Value.value();
            if (key == null || key.isEmpty()) key = field.getName();
        } else if(typeSchema.isExplicit() || !ISchemaValue.serializable(field.getType())) {
            return null;
        }

        SchemaValueAbs schemaValue;
        if(Collection.class.isAssignableFrom(field.getType())) {
            schemaValue = new SchemaFieldArray(typeSchema, field, key);
        } else if(Map.class.isAssignableFrom(field.getType())) {
            schemaValue = new SchemaFieldMap(typeSchema, field, key);
        }
        else {
            schemaValue = new SchemaFieldNormal(typeSchema, field, key);
        }

        return schemaValue;
    }

    static SchemaValueAbs of(TypeSchema typeSchema, Method method) {
        JSON5ValueGetter getter = method.getAnnotation(JSON5ValueGetter.class);
        JSON5ValueSetter setter = method.getAnnotation(JSON5ValueSetter.class);
        if(setter == null && getter == null) return null;
        if(SchemaMethodForArrayType.isCollectionTypeParameterOrReturns(method)) {
            return new SchemaMethodForArrayType(typeSchema, method);
        }
        else if(SchemaMethodForMapType.isMapTypeParameterOrReturns(method)) {
            return new SchemaMethodForMapType(typeSchema, method);
        }
        return new SchemaMethod(typeSchema, method);
    }


    boolean appendDuplicatedSchemaValue(SchemaValueAbs node) {
        if(node.parentsTypeSchema != this.parentsTypeSchema) {
            return false;
        }
        else if(node instanceof ISchemaArrayValue && !(this instanceof ISchemaArrayValue) ||
                !(node instanceof ISchemaArrayValue) && this instanceof ISchemaArrayValue) {
            //TODO 예외 발생 시켜야한다.
            return false;
        }
        else if(node instanceof ISchemaArrayValue && this instanceof ISchemaArrayValue) {
            ISchemaArrayValue nodeArray = (ISchemaArrayValue) node;
            ISchemaArrayValue thisArray = (ISchemaArrayValue) this;
            if(nodeArray.getCollectionItems().size() != thisArray.getCollectionItems().size()) {
                //TODO 예외 발생 시켜야한다.
                // TODO 이건 좀 고민중...
                //return false;
            }
        }
        this.allSchemaValueAbsList.add(node);
        return true;
    }

    @SuppressWarnings("unchecked")
    <T extends SchemaValueAbs> List<T> getAllSchemaValueList() {

        return (List<T>) this.allSchemaValueAbsList;
    }




    Object newInstance() {
        if(objectTypeSchema == null) return null;
        return objectTypeSchema.newInstance();
    }



    SchemaValueAbs(TypeSchema parentsTypeSchema, String path, Class<?> valueTypeClass, Type genericType) {

        this.path = path;
        this.valueTypeClass = valueTypeClass;
        this.parentsTypeSchema = parentsTypeSchema;
        this.isEnum = valueTypeClass.isEnum();

        Types type = Types.Object;
        if(genericType instanceof TypeVariable && parentsTypeSchema != null) {
            TypeVariable typeVariable = (TypeVariable)genericType;
            if(parentsTypeSchema.containsGenericType(typeVariable.getName())) {
                type = Types.GenericType;
            }
        } else {
            type = Types.of(valueTypeClass);
        }
        this.type = type;

        if(this.type == Types.Object || this.type == Types.AbstractObject) {
            try {
                this.objectTypeSchema = TypeSchemaMap.getInstance().getTypeInfo(valueTypeClass);
            } catch (JSON5SerializerException e) {
                throw new JSON5SerializerException("A type that cannot be used as a serialization object : " + valueTypeClass.getName() + ". (path: " + parentsTypeSchema.getType().getName() + "." + path + ")", e);
            }
        }
        else {
            this.objectTypeSchema = null;
        }

        this.isPrimitive = valueTypeClass.isPrimitive();
        this.allSchemaValueAbsList.add(this);
    }




    final Types types() {
        return type;
    }

    void changeType(Types type) {
        this.type = type;
    }

    boolean isPrimitive() {
        return isPrimitive;
    }



    final Types getType() {
        return type;
    }

    final int getId() {
        return id;
    }

    final String getPath() {
        return path;
    }

    final Class<?> getValueTypeClass() {
        return valueTypeClass;
    }

    @SuppressWarnings("unchecked")
    final <T extends SchemaValueAbs> T getParentField() {
        return (T) parentFieldRack;
    }




    final void setParentFiled(SchemaValueAbs parent) {
        this.parentFieldRack = parent;
    }


    @Override
    public Object getValue(Object parent) {
        // ValueProcessor를 사용하여 타입별로 최적화된 처리 수행
        ValueProcessor processor = ValueProcessorFactory.getInstance().getProcessor(this.type);
        return processor.getValue(parent, this);
    }


    /*
    2024.01.08 동일한 path 의 JSON5Element 가 여러개 있을 경우 merge 하도록 하는 코드.
     추후 이 것을 구현해야 하는 상황이 생긴다면 주석을 해제하여 사용한다.
    @Override
    public Object getValue(Object parent) {
        Object value = null;
        int index = this.allSchemaValueAbsList.size() - 1;
        boolean doContinue = true;
        JSON5Element lastJSON5Element = null;
        while(doContinue && index > -1) {
            SchemaValueAbs duplicatedSchemaValueAbs = this.allSchemaValueAbsList.get(index);
            if(type == Types.JSON5Element && duplicatedSchemaValueAbs.type != Types.JSON5Element) {
                continue;
            }

            value = duplicatedSchemaValueAbs.onGetValue(parent);
            if(value == null) {
                index--;
                continue;
            }
            if(!this.equalsValueType(duplicatedSchemaValueAbs)) {
                if(this instanceof ISchemaArrayValue || this instanceof ISchemaMapValue) {
                    return value;
                } else {
                    value = Utils.convertValue(value, duplicatedSchemaValueAbs.type);
                }
            }
            index--;
            if(type != Types.JSON5Element && value != null) {
                doContinue = false;
            } else if(value instanceof JSON5Element) {
                if(lastJSON5Element != null) {
                    if(lastJSON5Element instanceof  JSON5Object && value instanceof  JSON5Object) {
                        ((JSON5Object) lastJSON5Element).merge((JSON5Object) value);
                        value = lastJSON5Element;
                    }
                    else if(lastJSON5Element instanceof JSON5Array && value instanceof JSON5Array) {
                        ((JSON5Array) lastJSON5Element).merge((JSON5Array) value);
                        value = lastJSON5Element;
                    }
                } else {
                    lastJSON5Element = (JSON5Element) value;
                }
            }
        }
        return value;
    }*/

    @Override
    public void setValue(Object parent, Object value) {
        // ValueProcessor를 사용하여 타입별로 최적화된 처리 수행
        ValueProcessor processor = ValueProcessorFactory.getInstance().getProcessor(this.type);
        processor.setValue(parent, value, this);
    }


    abstract Object onGetValue(Object parent);

    abstract void onSetValue(Object parent, Object value);




    void onSetValue(Object parent, short value) {
        onSetValue(parent, Short.valueOf(value));
    }

    void onSetValue(Object parent, int value) {
         onSetValue(parent, Integer.valueOf(value));
    }

    void onSetValue(Object parent, long value) {
         onSetValue(parent, Long.valueOf(value));
    }

    void onSetValue(Object parent, float value) {
         setValue(parent, Float.valueOf(value));
    }

    void onSetValue(Object parent, double value) {
         onSetValue(parent,Double.valueOf(value));
    }

    void onSetValue(Object parent, boolean value) {
         onSetValue(parent,Boolean.valueOf(value));
    }

    void onSetValue(Object parent, char value) {
         onSetValue(parent,Character.valueOf(value));
    }

    void onSetValue(Object parent, byte value) {
         onSetValue(parent,Byte.valueOf(value));
    }


    boolean equalsValueType(SchemaValueAbs schemaValueAbs) {
        if(this.valueTypeClass == null) return false;
        return this.valueTypeClass.equals(schemaValueAbs.getValueTypeClass());
    }


    @Override
    public String toString() {
        return id + ""; /*"FieldRack{" +
                "id=" + id +
                ", field=" + field +
                ", path='" + path + '\'' +
                ", isPrimitive=" + isPrimitive +
                ", isByteArray=" + isByteArray +
                ", typeElement=" + typeElement +
                ", fieldType=" + fieldType +
                ", type=" + type +
                ", parentFieldRack=" + parentFieldRack +
                '}';*/
    }




}
