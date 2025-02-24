package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Array;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

class SchemaMethodForArrayType extends SchemaMethod implements ISchemaArrayValue {




    @SuppressWarnings("DuplicatedCode")
    static boolean isCollectionTypeParameterOrReturns(Method method) {
        JSON5ValueGetter getter = method.getAnnotation(JSON5ValueGetter.class);
        JSON5ValueSetter setter = method.getAnnotation(JSON5ValueSetter.class);
        if(getter != null && JSON5Array.class.isAssignableFrom(method.getReturnType())) {
            return false;
        }
        else if(getter != null && Collection.class.isAssignableFrom(method.getReturnType())) {
            return true;
        }
        Class<?>[] types = method.getParameterTypes();
        if(setter != null && types.length == 1) {
            if(JSON5Array.class.isAssignableFrom(types[0])) {
                return false;
            } else if(Collection.class.isAssignableFrom(types[0])) {
                return true;
            }
        }
        return false;
    }

    private final List<CollectionItems> collectionBundles;
    protected final Types endpointValueType;
    private final TypeSchema objectValueTypeSchema;


    SchemaMethodForArrayType(TypeSchema parentsTypeSchema, Method method) {
        super(parentsTypeSchema, method);


        boolean isGetter = getMethodType() == MethodType.Getter;
        Type genericFieldType = isGetter ? method.getGenericReturnType() : method.getGenericParameterTypes()[0];
        String methodPath = method.getDeclaringClass().getName() + "." + method.getName();
        if(isGetter) {
            methodPath += "() <return: " + method.getReturnType().getName() + ">";
        }
        else {
            methodPath += "(" + method.getParameterTypes()[0].getName() + ") <return: " + method.getReturnType().getName() + ">";
        }


        this.collectionBundles = ISchemaArrayValue.getGenericType(genericFieldType, methodPath);
        CollectionItems lastCollectionItems = this.collectionBundles.get(this.collectionBundles.size() - 1);
        Class<?> valueClass = lastCollectionItems.getValueClass();
        endpointValueType = lastCollectionItems.isGeneric() ? Types.GenericType : Types.of(valueClass);
        if (endpointValueType == Types.Object) {
            objectValueTypeSchema = TypeSchemaMap.getInstance().getTypeInfo(valueClass);
        } else {
            objectValueTypeSchema = null;
        }

    }



    @Override
    public Types getEndpointValueType() {
        return this.endpointValueType;
    }

    @Override
    public TypeSchema getObjectValueTypeElement() {
        return this.objectValueTypeSchema;
    }

    @Override
    public List<CollectionItems> getCollectionItems() {
        return collectionBundles;
    }


    @Override
    public boolean isAbstractType() {
        return endpointValueType == Types.AbstractObject;
    }


    @Override
    boolean equalsValueType(SchemaValueAbs schemaValueAbs) {
        if(!(schemaValueAbs instanceof ISchemaArrayValue)) {
            return false;
        }

        if(!ISchemaArrayValue.equalsCollectionTypes(this.getCollectionItems(), ((ISchemaArrayValue)schemaValueAbs).getCollectionItems())) {
            return false;
        }
        if(this.endpointValueType != ((ISchemaArrayValue)schemaValueAbs).getEndpointValueType()) {
            return false;
        }
        return super.equalsValueType(schemaValueAbs);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.METHOD_FOR_ARRAY;
    }

}
