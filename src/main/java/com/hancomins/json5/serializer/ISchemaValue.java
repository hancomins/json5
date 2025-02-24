package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Element;

public interface ISchemaValue extends ISchemaNode {

    Object getValue(Object parent);

    void setValue(Object parent, Object value);

    String getComment();
    String getAfterComment();

    boolean isAbstractType();

    static void assertValueType(Class<?> valueType, String parentPath) {
        assertValueType(valueType, Types.of(valueType), parentPath);
    }

    static void assertValueType(Class<?> valueType,Types type, String parentPath) {
        if(JSON5Element.class.isAssignableFrom(valueType)) {
            return;
        }

        if(valueType.isArray() && type != Types.ByteArray) {
            if(parentPath != null) {
                throw new JSON5ObjectException("Array type '" + valueType.getName() + "' is not supported");
            } else  {
                throw new JSON5ObjectException("Array type '" + valueType.getName() + "' of field '" + parentPath + "' is not supported");
            }
        }
        if(type == Types.Object && valueType.getAnnotation(JSON5Type.class) == null)  {
            if(parentPath != null) {
                throw new JSON5ObjectException("Object type '" + valueType.getName() + "' is not annotated with @JSON5Type");
            } else  {
                throw new JSON5ObjectException("Object type '" + valueType.getName() + "' of field '" + parentPath + "' is not annotated with @JSON5Type");
            }
        }
    }

    // 0.9.29
    static boolean serializable(Class<?> valueType) {
        if(JSON5Element.class.isAssignableFrom(valueType)) {
            return true;
        }
        Types type = Types.of(valueType);
        if(valueType.isArray() && type != Types.ByteArray) {
            return false;
        }
        return type != Types.Object || valueType.getAnnotation(JSON5Type.class) != null;
    }

}
