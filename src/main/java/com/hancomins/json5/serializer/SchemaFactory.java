package com.hancomins.json5.serializer;

import com.hancomins.json5.PathItem;
import com.hancomins.json5.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Schema 객체의 생성과 관리를 담당하는 팩토리 클래스입니다.
 * 
 * <p>이 클래스는 NodePath 클래스의 Schema 생성 로직을 캡슐화하여
 * 단일 책임 원칙을 준수하고 확장성을 높입니다.</p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>TypeSchema를 기반으로 SchemaObjectNode 생성</li>
 *   <li>경로 기반의 SchemaElementNode 생성</li>
 *   <li>JSON5Value 어노테이션이 붙은 필드/메소드 탐색</li>
 *   <li>상속 관계를 고려한 Schema 구성</li>
 * </ul>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class SchemaFactory {
    
    private static final SchemaFactory INSTANCE = new SchemaFactory();
    
    private final SchemaCache cache;
    
    private SchemaFactory() {
        this.cache = new DefaultSchemaCache();
    }
    
    /**
     * SchemaFactory의 싱글톤 인스턴스를 반환합니다.
     * 
     * @return SchemaFactory 인스턴스
     */
    public static SchemaFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * TypeSchema를 기반으로 SchemaObjectNode를 생성합니다.
     * 
     * <p>이 메소드는 기존 NodePath.makeSchema 로직을 캡슐화합니다.
     * 캐싱을 통해 동일한 타입에 대한 반복 생성을 방지합니다.</p>
     * 
     * @param targetTypeSchema 대상 타입 스키마
     * @param parentFieldRack 부모 필드 정보
     * @return 생성된 SchemaObjectNode
     */
    public SchemaObjectNode createSchema(TypeSchema targetTypeSchema, SchemaValueAbs parentFieldRack) {
        // 캐시를 일시적으로 비활성화하여 문제 해결
        /*
        if (parentFieldRack == null) {
            SchemaObjectNode cached = cache.getCachedSchema(targetTypeSchema.getType());
            if (cached != null) {
                return cached;
            }
        }
        */
        
        List<SchemaValueAbs> fieldRacks = searchAllJSON5ValueFields(targetTypeSchema, targetTypeSchema.getType());
        SchemaObjectNode objectNode = new SchemaObjectNode().setBranchNode(false);
        
        for (SchemaValueAbs fieldRack : fieldRacks) {
            fieldRack.setParentFiled(parentFieldRack);
            String path = fieldRack.getPath();
            
            if (fieldRack.getType() == Types.Object) {
                TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(fieldRack.getValueTypeClass());
                SchemaObjectNode childTree = createSchema(typeSchema, fieldRack);
                childTree.addParentFieldRack(fieldRack);
                childTree.setBranchNode(false);
                
                // Comment 정보를 childTree에 설정
                if (fieldRack instanceof ISchemaValue) {
                    ISchemaValue schemaValue = (ISchemaValue) fieldRack;
                    childTree.setComment(schemaValue.getComment());
                    childTree.setAfterComment(schemaValue.getAfterComment());
                }
                
                SchemaElementNode elementNode = createSubTree(path, childTree);
                elementNode.setBranchNode(false);
                objectNode.merge(elementNode);
                continue;
            }
            
            SchemaElementNode elementNode = createSubTree(path, fieldRack);
            objectNode.merge(elementNode);
        }
        
        if (parentFieldRack == null) {
            objectNode.setBranchNode(false);
            // 캐시를 일시적으로 비활성화
            // cache.putSchema(targetTypeSchema.getType(), objectNode);
        }
        
        return objectNode;
    }
    
    /**
     * 경로 기반으로 SchemaElementNode를 생성합니다.
     * 
     * <p>이 메소드는 기존 NodePath.makeSubTree 로직을 캡슐화합니다.</p>
     * 
     * @param path 생성할 경로
     * @param value 경로 끝에 배치할 값
     * @return 생성된 SchemaElementNode
     */
    public SchemaElementNode createSubTree(String path, ISchemaNode value) {
        // 캐시를 일시적으로 비활성화
        /*
        String cacheKey = path + ":" + value.getClass().getSimpleName();
        SchemaElementNode cached = cache.getCachedSubTree(cacheKey);
        if (cached != null) {
            return cached;
        }
        */
        
        List<PathItem> list = PathItem.parseMultiPath2(path);
        SchemaElementNode rootNode = new SchemaObjectNode();
        SchemaElementNode schemaNode = rootNode;
        
        for (int i = 0, n = list.size(); i < n; ++i) {
            PathItem pathItem = list.get(i);
            if (pathItem.isEndPoint()) {
                putNode(schemaNode, pathItem, value);
                break;
            }
            schemaNode = obtainOrCreateChild(schemaNode, pathItem);
        }
        
        // 캐시를 일시적으로 비활성화
        // cache.putSubTree(cacheKey, rootNode);
        
        return rootNode;
    }
    
    /**
     * 현재 사용 중인 SchemaCache를 반환합니다.
     * 
     * @return 현재 SchemaCache 인스턴스
     */
    public SchemaCache getCache() {
        return cache;
    }
    
    /**
     * 캐시를 무효화합니다.
     */
    public void invalidateCache() {
        cache.invalidateCache();
    }
    
    // ===== Private Helper Methods =====
    
    private List<SchemaValueAbs> searchAllJSON5ValueFields(TypeSchema typeSchema, Class<?> clazz) {
        List<SchemaValueAbs> results = new ArrayList<>();
        findSchemaByAncestors(typeSchema, results, clazz);
        
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces != null) {
            for (Class<?> interfaceClass : interfaces) {
                findSchemaByAncestors(typeSchema, results, interfaceClass);
            }
        }
        
        return results;
    }
    
    private void findSchemaByAncestors(TypeSchema typeSchema, List<SchemaValueAbs> results, Class<?> currentClass) {
        List<Field> fields = ReflectionUtils.getAllInheritedFields(currentClass);
        List<Method> methods = ReflectionUtils.getAllInheritedMethods(currentClass);
        findJSON5ValueFields(typeSchema, results, fields);
        findJSON5GetterSetterMethods(typeSchema, results, methods);
    }
    
    private void findJSON5GetterSetterMethods(TypeSchema typeSchema, List<SchemaValueAbs> results, List<Method> methods) {
        if (methods != null) {
            for (Method method : methods) {
                SchemaMethod methodRack = (SchemaMethod) SchemaValueAbs.of(typeSchema, method);
                if (methodRack != null) {
                    results.add(methodRack);
                }
            }
        }
    }
    
    private void findJSON5ValueFields(TypeSchema typeSchema, List<SchemaValueAbs> results, List<Field> fields) {
        if (fields != null) {
            for (Field field : fields) {
                SchemaValueAbs fieldRack = SchemaValueAbs.of(typeSchema, field);
                if (fieldRack != null) {
                    results.add(fieldRack);
                }
            }
        }
    }
    
    private SchemaElementNode obtainOrCreateChild(SchemaElementNode Node, PathItem pathItem) {
        if (Node instanceof SchemaObjectNode && !pathItem.isInArray() && pathItem.isObject()) {
            SchemaObjectNode ObjectNode = (SchemaObjectNode) Node;
            String name = pathItem.getName();
            if (pathItem.isArrayValue()) {
                SchemaArrayNode childArrayNode = ObjectNode.getArrayNode(name);
                if (childArrayNode == null) {
                    childArrayNode = new SchemaArrayNode();
                    ObjectNode.put(name, childArrayNode);
                }
                return childArrayNode;
            } else {
                SchemaObjectNode childObjectNode = ObjectNode.getObjectNode(name);
                if (childObjectNode == null) {
                    childObjectNode = new SchemaObjectNode();
                    ObjectNode.put(name, childObjectNode);
                }
                return childObjectNode;
            }
        } else if (Node instanceof SchemaArrayNode && pathItem.isInArray()) {
            SchemaArrayNode ArrayNode = (SchemaArrayNode) Node;
            int index = pathItem.getIndex();
            if (pathItem.isObject()) {
                SchemaObjectNode childObjectNode = ArrayNode.getObjectNode(index);
                if (childObjectNode == null) {
                    childObjectNode = new SchemaObjectNode();
                    ArrayNode.put(index, childObjectNode);
                    if (pathItem.isArrayValue()) {
                        SchemaArrayNode childArrayNode = new SchemaArrayNode();
                        childObjectNode.put(pathItem.getName(), childArrayNode);
                        return childArrayNode;
                    }
                    SchemaObjectNode childAndChildObjectNode = new SchemaObjectNode();
                    childObjectNode.put(pathItem.getName(), childAndChildObjectNode);
                    return childAndChildObjectNode;
                } else {
                    if (pathItem.isArrayValue()) {
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
            } else if (pathItem.isArrayValue()) {
                SchemaArrayNode childArrayNode = ArrayNode.getArrayNode(index);
                if (childArrayNode == null) {
                    childArrayNode = new SchemaArrayNode();
                    ArrayNode.put(index, childArrayNode);
                }
                return childArrayNode;
            }
            
            throw new IllegalArgumentException("Invalid path");
        } else {
            throw new IllegalArgumentException("Invalid path");
        }
    }
    
    private void putNode(ISchemaNode Node, PathItem pathItem, ISchemaNode value) {
        if (pathItem.isInArray()) {
            if (pathItem.isObject()) {
                int index = pathItem.getIndex();
                SchemaObjectNode childObjectNode = ((SchemaArrayNode) Node).getObjectNode(index);
                if (childObjectNode == null) {
                    childObjectNode = new SchemaObjectNode();
                    ((SchemaArrayNode) Node).put(index, childObjectNode);
                }
                childObjectNode.put(pathItem.getName(), value);
                
                // Comment 정보 전파
                if (value instanceof SchemaObjectNode && ((SchemaObjectNode) value).getComment() != null) {
                    childObjectNode.setComment(((SchemaObjectNode) value).getComment());
                    childObjectNode.setAfterComment(((SchemaObjectNode) value).getAfterComment());
                }
            } else {
                ((SchemaArrayNode) Node).put(pathItem.getIndex(), value);
            }
        } else {
            ((SchemaObjectNode) Node).put(pathItem.getName(), value);
            
            // Comment 정보 전파
            if (value instanceof SchemaObjectNode) {
                SchemaObjectNode objNode = (SchemaObjectNode) value;
                if (objNode.getComment() != null) {
                    ((SchemaObjectNode) Node).setComment(objNode.getComment());
                    ((SchemaObjectNode) Node).setAfterComment(objNode.getAfterComment());
                }
            }
        }
    }
}
