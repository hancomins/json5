package com.hancomins.json5.serializer;


import java.lang.reflect.Field;

public class SchemaFieldNormal extends SchemaField {


    protected SchemaFieldNormal(TypeSchema typeSchema, Field field, String path) {
        super(typeSchema, field, path);

        if(this.types() != Types.JSON5Object && this.types() != Types.JSON5Array &&  this.types() == Types.Object && getField().getType().getAnnotation(JSON5Type.class) == null)  {
            throw new JSON5SerializerException("Object type " + this.field.getType().getName() + " is not annotated with @JSON5Type");
        }
    }


    public SchemaFieldNormal copy() {
        SchemaFieldNormal fieldRack = new SchemaFieldNormal(parentsTypeSchema, field, path);
        fieldRack.setParentFiled(getParentField());
        return fieldRack;
    }



    @Override
    public ISchemaNode copyNode() {
        SchemaFieldNormal fieldRack = copy();
        return fieldRack;
    }

    @Override
    public String targetPath() {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }

    @Override
    public boolean isIgnoreError() {
        return obtainTypeValueInvoker != null && obtainTypeValueInvoker.isIgnoreError();
    }

    @Override
    public boolean isAbstractType() {
        return types() == Types.AbstractObject;
    }

    /*

    @Override
    public Object newInstance(JSON5Element json5Element) {
        String fieldName = getField().getName();
        /*if(type == Types.Object) {
            ObtainTypeValueInvoker rack = parentsTypeSchema.findObjectObrainorRack(fieldName);
            if(rack != null) {
                rack.obtain(json5Element);

            }
        }*/


    @Override
    public NodeType getNodeType() {
        return NodeType.NORMAL_FIELD;
    }

}
