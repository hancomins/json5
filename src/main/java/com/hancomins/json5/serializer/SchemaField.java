package com.hancomins.json5.serializer;

import com.hancomins.json5.util.DataConverter;

import java.lang.reflect.Field;

public abstract class SchemaField extends SchemaValueAbs implements ObtainTypeValueInvokerGetter {

    final Field field;
    final String fieldName;

    final String comment;
    final String afterComment;
    private final boolean isStatic;

    final ObtainTypeValueInvoker obtainTypeValueInvoker;

    //private final boolean isMapField;


    SchemaField(TypeSchema parentsTypeSchema, Field field, String path) {
        super(parentsTypeSchema, path, field.getType(), field.getGenericType());
        this.field = field;
        this.fieldName = field.getName();
        try {
            field.setAccessible(true);
        } catch (Exception e) {
            // Java 9+ 모듈 시스템에서 일부 기본 모듈의 필드 접근이 제한될 수 있음
            // 이 경우 field.setAccessible(true) 호출을 건너뛰고 계속 진행
        }
        this.isStatic = java.lang.reflect.Modifier.isStatic(field.getModifiers());

        obtainTypeValueInvoker = parentsTypeSchema.findObtainTypeValueInvoker(fieldName);


        JSON5Value json5Value = field.getAnnotation(JSON5Value.class);
        if(json5Value != null) {
            String comment = json5Value.comment();
            String afterComment = json5Value.commentAfterKey();
            this.comment = comment.isEmpty() ? null : comment;
            this.afterComment = afterComment.isEmpty() ? null : afterComment;
            ISchemaValue.assertValueType(field.getType(), this.getType(), field.getDeclaringClass().getName() + "." + field.getName() );
        } else {
            this.comment = null;
            this.afterComment = null;
        }


    }


    @Override
    public ObtainTypeValueInvoker getObtainTypeValueInvoker() {
        return obtainTypeValueInvoker;
    }




    @Override
    public String getComment() {
        return comment;
    }
    @Override
    public String getAfterComment() {
        return afterComment;
    }


    Field getField() {
        return field;
    }


    @Override
    Object onGetValue(Object parent) {
        if(isStatic) parent = null;
        try {
            Object value = field.get(parent);
            if(isEnum && value != null) {
                return value.toString();
            }
            return value;
        } catch (IllegalAccessException e) {

            return null;
        }
    }
    @SuppressWarnings("unchecked")
    @Override
    void onSetValue(Object parent, Object value) {
        if(isStatic) parent = null;
        try {
            if(isEnum) {
                try {
                    value = Enum.valueOf((Class<Enum>) valueTypeClass, value.toString());
                } catch (Exception e) {
                    value = null;
                }
            }
            if(value != null && !valueTypeClass.isAssignableFrom(value.getClass()) ) {
                value = DataConverter.convertValue(valueTypeClass, value);
                field.set(parent, value);
            } else {
                field.set(parent, value);
            }
        } catch (IllegalAccessException e) {
            throw new JSON5SerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }





    @Override
    void onSetValue(Object parent, short value) {
        if(isStatic) parent = null;
        try {
            field.setShort(parent, value);
        } catch (IllegalAccessException e) {
            throw new JSON5SerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }
    @Override
    void onSetValue(Object parent, int value) {
        if(isStatic) parent = null;
        try {
            field.setInt(parent, value);
        } catch (IllegalAccessException e) {
            throw new JSON5SerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }
    @Override
    void onSetValue(Object parent, long value) {
        if(isStatic) parent = null;
        try {
            field.setLong(parent, value);
        } catch (IllegalAccessException e) {
            throw new JSON5SerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }
    @Override
    void onSetValue(Object parent, float value) {
        if(isStatic) parent = null;
        try {
            field.setFloat(parent, value);
        } catch (IllegalAccessException e) {
            throw new JSON5SerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }
    @Override
    void onSetValue(Object parent, double value) {
        if(isStatic) parent = null;
        try {
            field.setDouble(parent, value);
        } catch (IllegalAccessException e) {
            throw new JSON5SerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }
    @Override
    void onSetValue(Object parent, boolean value) {
        if(isStatic) parent = null;
        try {
            field.setBoolean(parent, value);
        } catch (IllegalAccessException e) {
            throw new JSON5SerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }
    @Override
    void onSetValue(Object parent, char value) {
        if(isStatic) parent = null;
        try {
            field.setChar(parent, value);
        } catch (IllegalAccessException e) {
            throw new JSON5SerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }

    @Override
    void onSetValue(Object parent, byte value) {
        if(isStatic) parent = null;
        try {
            field.setByte(parent, value);
        } catch (IllegalAccessException e) {
            throw new JSON5SerializerException("Failed to set value to field. " + field.getDeclaringClass().getName() + "." + field.getName(), e);
        }
    }



    @Override
    public String toString() {
        return getId() + ""; /*"FieldRack{" +
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
