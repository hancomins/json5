package com.hancomins.json5.serializer;


import com.hancomins.json5.PathItem;
import com.hancomins.json5.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.*;

public class NodePath {

    private final SchemaElementNode node;

    protected NodePath(SchemaElementNode Node) {
        this.node = Node;
    }


    public static SchemaObjectNode makeSchema(TypeSchema targetTypeSchema, SchemaValueAbs parentFieldRack) {
        // SchemaFactory를 사용하여 Schema 생성 위임
        return SchemaFactory.getInstance().createSchema(targetTypeSchema, parentFieldRack);
    }


    private static List<SchemaValueAbs> searchAllJSON5ValueFields(TypeSchema typeSchema, Class<?> clazz) {
        //Set<String> fieldPaths = new HashSet<>();
        List<SchemaValueAbs> results = new ArrayList<>();
        findSchemaByAncestors(typeSchema, results, clazz);
        Class<?>[] interfaces = clazz.getInterfaces();
        if(interfaces != null) {
            for(Class<?> interfaceClass : interfaces) {
                findSchemaByAncestors(typeSchema, results, interfaceClass);
            }
        }
        return results;
    }

    private static void findSchemaByAncestors(TypeSchema typeSchema, List<SchemaValueAbs> results, Class<?> currentClass) {
        List<Field> fields = ReflectionUtils.getAllInheritedFields(currentClass);
        List<Method> methods = ReflectionUtils.getAllInheritedMethods(currentClass);
        findJSON5ValueFields(typeSchema, results, fields);
        findJSON5GetterSetterMethods(typeSchema, results, methods);

    }


    private static void findJSON5GetterSetterMethods(TypeSchema typeSchema, List<SchemaValueAbs> results, List<Method> methods) {
        if(methods != null) {
            for(Method method : methods) {
                SchemaMethod methodRack = (SchemaMethod)SchemaValueAbs.of(typeSchema,method);
                if(methodRack != null) {
                    results.add(methodRack);
                }
            }
        }
    }

    private static void findJSON5ValueFields(TypeSchema typeSchema, List<SchemaValueAbs> results, List<Field> fields) {
        if(fields != null) {
            for (Field field : fields) {
                SchemaValueAbs fieldRack = SchemaValueAbs.of(typeSchema, field);
                if (fieldRack != null  /* && !fieldPaths.contains(fieldRack.getPath()) */) {
                    results.add(fieldRack);
                }
            }
        }
    }



    private static SchemaElementNode obtainOrCreateChild(SchemaElementNode Node, PathItem pathItem) {
        if(Node instanceof SchemaObjectNode && !pathItem.isInArray() && pathItem.isObject()) {
            SchemaObjectNode ObjectNode = (SchemaObjectNode)Node;
            String name = pathItem.getName();
            if(pathItem.isArrayValue()) {
                SchemaArrayNode childArrayNode = ObjectNode.getArrayNode(name);
                if(childArrayNode == null) {
                    childArrayNode = new SchemaArrayNode();
                    ObjectNode.put(name, childArrayNode);
                }

                return childArrayNode;
            } else {
                SchemaObjectNode childObjectNode = ObjectNode.getObjectNode(name);
                if(childObjectNode == null) {
                    childObjectNode = new SchemaObjectNode();
                    ObjectNode.put(name, childObjectNode);
                }
                return childObjectNode;
            }
        } else if(Node instanceof SchemaArrayNode && pathItem.isInArray()) {
            SchemaArrayNode ArrayNode = (SchemaArrayNode)Node;
            int index = pathItem.getIndex();
            if(pathItem.isObject()) {
                SchemaObjectNode childObjectNode = ArrayNode.getObjectNode(index);
                if(childObjectNode == null) {
                    childObjectNode = new SchemaObjectNode();
                    ArrayNode.put(index, childObjectNode);
                    if(pathItem.isArrayValue()) {
                        SchemaArrayNode childArrayNode = new SchemaArrayNode();
                        childObjectNode.put(pathItem.getName(), childArrayNode);
                        return childArrayNode;
                    }
                    SchemaObjectNode childAndChildObjectNode = new SchemaObjectNode();
                    childObjectNode.put(pathItem.getName(), childAndChildObjectNode);
                    return childAndChildObjectNode;
                } else  {
                    if(pathItem.isArrayValue()) {
                        SchemaArrayNode childChildArrayNode = childObjectNode.getArrayNode(pathItem.getName());
                        if (childChildArrayNode == null) {
                            childChildArrayNode = new SchemaArrayNode();
                            childObjectNode.put(pathItem.getName(), childChildArrayNode);
                        }
                        return childChildArrayNode;
                    } else {
                        SchemaObjectNode childAndChildObjectNode = childObjectNode.getObjectNode(pathItem.getName());
                        if (childAndChildObjectNode == null) {
                            childAndChildObjectNode = new SchemaObjectNode();
                            childObjectNode.put(pathItem.getName(), childAndChildObjectNode);
                        }
                        return childAndChildObjectNode;
                    }
                }
            }
            else if(pathItem.isArrayValue()) {
                SchemaArrayNode childArrayNode = ArrayNode.getArrayNode(index);
                if(childArrayNode == null) {
                    childArrayNode = new SchemaArrayNode();
                    ArrayNode.put(index, childArrayNode);
                }
                return childArrayNode;
            }

            // TODO 에러를 뿜어야함..
            //throw new RuntimeException("Invalid path");
            throw new IllegalArgumentException("Invalid path");
        } else {
            //TODO 에러를 뿜어야함..
            //throw new RuntimeException("Invalid path");
            throw new IllegalArgumentException("Invalid path");
        }
    }



    private static void putNode(ISchemaNode Node, PathItem pathItem, ISchemaNode value) {
        if(pathItem.isInArray()) {
            if(pathItem.isObject()) {
                int index = pathItem.getIndex();
                SchemaObjectNode childObjectNode = ((SchemaArrayNode)Node).getObjectNode(index);
                if(childObjectNode == null) {
                    childObjectNode = new SchemaObjectNode();
                    ((SchemaArrayNode)Node).put(index, childObjectNode);
                }
                childObjectNode.put(pathItem.getName(), value);
            } else {
                ((SchemaArrayNode)Node).put(pathItem.getIndex(), value);
            }
        } else {
            ((SchemaObjectNode)Node).put(pathItem.getName(), value);
        }
    }


    public static SchemaElementNode makeSubTree(String path, ISchemaNode value) {
        // SchemaFactory를 사용하여 SubTree 생성 위임
        return SchemaFactory.getInstance().createSubTree(path, value);
    }


    public Object get(String path) {
        // JSON5PathExtractor와 동일한 파싱 방식 사용하여 일관성 확보
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        
        try {
            List<PathItem> pathItemList = PathItem.parseMultiPath2(path);
            Object parents = node;
            
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0, n = pathItemList.size(); i < n; ++i) {
                PathItem pathItem = pathItemList.get(i);
                if (pathItem.isEndPoint()) {
                    if (pathItem.isInArray()) {
                        if(pathItem.isObject()) {
                            SchemaObjectNode endPointObject = ((SchemaArrayNode) parents).getObjectNode(pathItem.getIndex());
                            if(endPointObject == null) return null;
                            return endPointObject.get(pathItem.getName());
                        }
                        else {
                            return ((SchemaArrayNode)parents).get(pathItem.getIndex());
                        }
                    } else {
                        return ((SchemaObjectNode) parents).get(pathItem.getName());
                    }
                }
                else if((parents instanceof SchemaObjectNode && pathItem.isInArray()) || (parents instanceof SchemaArrayNode && !pathItem.isInArray())) {
                    return null;
                }
                else {
                    if (pathItem.isInArray()) {
                        assert parents instanceof SchemaArrayNode;
                        parents = ((SchemaArrayNode) parents).get(pathItem.getIndex());
                        if(pathItem.isObject() && parents instanceof SchemaObjectNode) {
                            parents = ((SchemaObjectNode) parents).get(pathItem.getName());
                        }
                    } else {
                        assert parents instanceof SchemaObjectNode;
                        parents = ((SchemaObjectNode) parents).get(pathItem.getName());
                    }
                    if(parents == null) return null;
                }
            }
            return null;
        } catch (Exception e) {
            // 파싱 실패 시 null 반환 (기존 동작 유지)
            return null;
        }
    }
    
    /**
     * 경로가 존재하는지 확인합니다.
     * JSON5PathExtractor와 일관된 인터페이스 제공
     */
    public boolean has(String path) {
        return get(path) != null;
    }

}
