package com.hancomins.json5.serializer;


import com.hancomins.json5.util.DataConverter;

import java.lang.reflect.Method;
import java.util.List;


class SchemaMethod extends SchemaValueAbs implements ObtainTypeValueInvokerGetter {



    private  ObtainTypeValueInvoker obtainTypeValueInvoker;

    private static Class<?> getValueType(Method method) {
        JSON5ValueGetter json5ValueGetter = method.getAnnotation(JSON5ValueGetter.class);
        JSON5ValueSetter json5ValueSetter = method.getAnnotation(JSON5ValueSetter.class);

        Class<?>[] types =  method.getParameterTypes();
        if(json5ValueSetter != null && json5ValueGetter != null) {
            throw new JSON5SerializerException("Method " + method.getDeclaringClass().getName() + "." + method.getName() + "(..) must be annotated with @JSON5ValueGetter or @JSON5ValueSetter, not both");
        }



        if(json5ValueSetter != null) {
            if(types.length != 1) {
                throw new JSON5SerializerException("Setter method " + method.getDeclaringClass().getName() + "." + method.getName() + "(..) must have only one parameter");
            }
            return types[0];
        }
        else if(json5ValueGetter != null) {
            Class<?> returnType = method.getReturnType();
            if(returnType == void.class || returnType == Void.class || returnType == null) {
                throw new JSON5SerializerException("Getter method " + method.getDeclaringClass().getName() + "." + method.getName() + "(..) must have return type");
            }
            if(types.length != 0) {
                throw new JSON5SerializerException("Getter method " + method.getDeclaringClass().getName() + "." + method.getName() + "(..) must have no parameter");
            }

            return returnType;
        }
        else {
            throw new JSON5SerializerException("Method " + method.getDeclaringClass().getName() + "." + method.getName() + "(..) must be annotated with @JSON5ValueGetter or @JSON5ValueSetter");
        }
    }


    private static String getPath(Method method) {
        JSON5ValueGetter json5ValueGetter = method.getAnnotation(JSON5ValueGetter.class);
        JSON5ValueSetter json5ValueSetter = method.getAnnotation(JSON5ValueSetter.class);
        if(json5ValueSetter != null) {
            String path = json5ValueSetter.value().trim();
            if(path.isEmpty()) {
                path = json5ValueSetter.key().trim();
            }
            if(path.isEmpty()) {
                path = setterNameFilter(method.getName());
            }
            return path.trim();
        }
        else if(json5ValueGetter != null) {
            String path = json5ValueGetter.value().trim();
            if(path.isEmpty()) {
                path = json5ValueGetter.key().trim();
            }
            if(path.isEmpty()) {
                path = getterNameFilter(method.getName()).trim();
            }
            return path.trim();
        }
        else {
            throw new JSON5SerializerException("Method " + method.getDeclaringClass().getName() + "." + method.getName() + " must be annotated with @JSON5ValueGetter or @JSON5ValueSetter");
        }
    }

    private static String setterNameFilter(String methodName) {
        if(methodName.length() > 3 && (methodName.startsWith("set") || methodName.startsWith("Set") || methodName.startsWith("SET") ||
                methodName.startsWith("put") || methodName.startsWith("Put") || methodName.startsWith("PUT") ||
                methodName.startsWith("add") || methodName.startsWith("Add") || methodName.startsWith("ADD"))) {
            String name = methodName.substring(3);
            name = name.substring(0,1).toLowerCase() + name.substring(1);
            return name;
        }
        else {
            return methodName;
        }
    }

    private static java.lang.reflect.Type getGenericType(Method method) {
        JSON5ValueGetter json5ValueGetter = method.getAnnotation(JSON5ValueGetter.class);
        JSON5ValueSetter json5ValueSetter = method.getAnnotation(JSON5ValueSetter.class);
        if(json5ValueSetter != null) {
            return method.getParameters()[0].getParameterizedType();
        }
        else if(json5ValueGetter != null) {
            return method.getGenericReturnType();
        }
        else {
            throw new JSON5SerializerException("Method " + method.getDeclaringClass().getName() + "." + method.getName() + "(..) must be annotated with @JSON5ValueGetter or @JSON5ValueSetter");
        }

    }

    static String getterNameFilter(String methodName) {
        if(methodName.length() > 3 && (methodName.startsWith("get") || methodName.startsWith("Get") || methodName.startsWith("GET"))) {
            String name =  methodName.substring(3);
            name = name.substring(0,1).toLowerCase() + name.substring(1);
            return name;
        }
        else if(methodName.length() > 3 && (methodName.startsWith("is") || methodName.startsWith("Is") || methodName.startsWith("IS"))) {
            String name = methodName.substring(2);
            name = name.substring(0,1).toLowerCase() + name.substring(1);
            return name;
        }
        else {
            return methodName;
        }
    }

    @Override
    public ObtainTypeValueInvoker getObtainTypeValueInvoker() {
        if(obtainTypeValueInvoker == null) {
            System.out.println(methodGetter.getName());
            System.out.println(methodSetter.getName());
        }

        if(obtainTypeValueInvoker != null) {
            return obtainTypeValueInvoker;
        }

        List<SchemaMethod> schemaMethods = getAllSchemaValueList();
        for(SchemaMethod schemaMethod : schemaMethods) {
            if(schemaMethod.obtainTypeValueInvoker != null) {
                return schemaMethod.obtainTypeValueInvoker;
            }
        }
        return null;

    }

    @Override
    public String targetPath() {
        if(methodSetter == null && methodGetter != null) {
            return methodGetter.getDeclaringClass().getName() + ".(Undeclared Setter)()";
        } else if(methodSetter == null && methodGetter == null) {
            return parentsTypeSchema.getType().getName() + ".(Undeclared Setter)()";
        }
       return methodSetter.getDeclaringClass().getName() + "." + methodSetter.getName() + "()";
    }

    @Override
    public boolean isIgnoreError() {
        return obtainTypeValueInvoker != null && obtainTypeValueInvoker.isIgnoreError();
    }

    static enum MethodType {
        Getter,
        Setter,
        Both

    }

    private final String methodPath;



    private static MethodType getMethodType(Method method) {
        JSON5ValueGetter json5ValueGetter = method.getAnnotation(JSON5ValueGetter.class);
        JSON5ValueSetter json5ValueSetter = method.getAnnotation(JSON5ValueSetter.class);

        if(json5ValueSetter != null) {
            return MethodType.Setter;
        }
        else if(json5ValueGetter != null) {
            return MethodType.Getter;
        }
        else {
            throw new JSON5SerializerException("Method " + method.getDeclaringClass().getName() + "." + method.getName() + " must be annotated with @JSON5ValueGetter or @JSON5ValueSetter");
        }
    }




    private MethodType methodType = null;
    private Method methodSetter;
    private Method methodGetter;

    private String comment = null;
    private String afterComment = null;

    private final boolean ignoreError;
    private final boolean isStatic;


    SchemaMethod(TypeSchema parentsTypeSchema, Method method) {
        super(parentsTypeSchema,getPath(method), getValueType(method), getGenericType(method));
        this.isStatic = java.lang.reflect.Modifier.isStatic(method.getModifiers());


        method.setAccessible(true);
        MethodType methodType = getMethodType(method);

        boolean isGetter = methodType == MethodType.Getter;
        String methodPath = method.getDeclaringClass().getName() + "." + method.getName();
        if(isGetter) {
            methodPath += "() <return: " + method.getReturnType().getName() + ">";
            ignoreError = method.getAnnotation(JSON5ValueGetter.class).ignoreError();
        }
        else {
            methodPath += "(" + method.getParameterTypes()[0].getName() + ") <return: " + method.getReturnType().getName() + ">";
            ignoreError = method.getAnnotation(JSON5ValueSetter.class).ignoreError();
            obtainTypeValueInvoker = parentsTypeSchema.findObtainTypeValueInvoker(method.getName());
        }
        this.methodPath = methodPath;

        if(this.getType() != Types.GenericType) {
            ISchemaValue.assertValueType(getValueTypeClass(), method.getDeclaringClass().getName() + "." + method.getName());
        }
        if(methodType == MethodType.Getter) {
            setGetter(method);
        }
        else if(methodType == MethodType.Setter) {
            setSetter(method);
        }
    }



    private void setGetter(Method method) {
        JSON5ValueGetter json5ValueGetter = method.getAnnotation(JSON5ValueGetter.class);
        String comment = json5ValueGetter.comment();
        String afterComment = json5ValueGetter.commentAfterKey();
        this.comment = comment.isEmpty() ? null : comment;
        this.afterComment = afterComment.isEmpty() ? null : afterComment;
        methodGetter = method;
        if(methodType == MethodType.Setter) {
            this.methodType = MethodType.Both;
        } else {
            this.methodType = MethodType.Getter;
        }
    }

    private void setSetter(Method method) {
        methodSetter = method;
        if(methodType == MethodType.Getter) {
            this.methodType = MethodType.Both;
        } else {
            this.methodType = MethodType.Setter;
        }
        if(obtainTypeValueInvoker == null) {
            obtainTypeValueInvoker = parentsTypeSchema.findObtainTypeValueInvoker(method.getName());
        }

    }




    @Override
    boolean appendDuplicatedSchemaValue(SchemaValueAbs node) {
        if(this.methodType != MethodType.Both &&  node instanceof SchemaMethod &&
                this.parentsTypeSchema == node.parentsTypeSchema &&
                this.valueTypeClass == node.valueTypeClass) {

            if(node instanceof SchemaMethodForMapType &&
                    this instanceof SchemaMethodForMapType &&
                    !((SchemaMethodForMapType) node).getElementType().equals(((SchemaMethodForMapType) this).getElementType())
            ) {
                return super.appendDuplicatedSchemaValue(node);
            } else if(node instanceof SchemaMethodForArrayType &&
                    this instanceof SchemaMethodForArrayType &&
                    !((SchemaMethodForArrayType) node).equalsValueType(this))
             {
                return super.appendDuplicatedSchemaValue(node);
            }

            SchemaMethod schemaMethod = (SchemaMethod) node;
            if(schemaMethod.methodType == MethodType.Getter && this.methodType == MethodType.Setter) {
                setGetter(schemaMethod.methodGetter);
                return true;
            } else if(schemaMethod.methodType == MethodType.Setter && this.methodType == MethodType.Getter) {
                setSetter(schemaMethod.methodSetter);
                return true;
            }
        }

        return super.appendDuplicatedSchemaValue(node);
    }



    public MethodType getMethodType() {
        return methodType;
    }

    @Override
    public ISchemaNode copyNode() {
        return new SchemaMethod(parentsTypeSchema, methodSetter);
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String getAfterComment() {
        return afterComment;
    }

    @Override
    public boolean isAbstractType() {
        return types() == Types.AbstractObject;
    }

    @Override
    Object onGetValue(Object parent) {
        if(methodGetter == null) return null;
        if(isStatic) parent = null;
        try {
            Object value = methodGetter.invoke(parent);
            if(isEnum && value != null) {
                return value.toString();
            }
            return value;

        } catch (Exception e) {
            if(ignoreError) {
                return null;
            }
            throw new JSON5SerializerException("Failed to invoke method " + this.methodPath, e);
        }
    }

    @Override
    void onSetValue(Object parent, Object value) {
        if(methodSetter == null) return;
        if(isStatic) parent = null;
        try {
            if(isEnum) {
                try {
                    //noinspection unchecked
                    value = Enum.valueOf((Class<Enum>) valueTypeClass, value.toString());
                } catch (Exception e) {
                    value = null;
                }
            }
            if(value != null && !valueTypeClass.isAssignableFrom(value.getClass()) ) {
                value = DataConverter.convertValue(valueTypeClass, value);
                methodSetter.invoke(parent, value);
            } else {
                methodSetter.invoke(parent, value);
            }
        } catch (Exception e) {
            if(ignoreError) {
                return;
            }
            throw new JSON5SerializerException("Failed to invoke method " + this.methodPath, e);
        }
    }





    static boolean isSchemaMethodGetter(ISchemaNode schemaValue) {
        return schemaValue.getClass() == SchemaMethod.class && (((SchemaMethod)schemaValue).getMethodType() == SchemaMethod.MethodType.Getter  || ((SchemaMethod)schemaValue).getMethodType() == SchemaMethod.MethodType.Both);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.METHOD;
    }

}
