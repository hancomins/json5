package com.hancomins.json5.serializer;

import com.hancomins.json5.*;

import java.util.*;

/**
 * JSON5 역직렬화 과정에서 상태 정보를 관리하는 컨텍스트 클래스
 * 
 * SerializationContext와 유사하게 역직렬화 과정에서 필요한 상태 정보를
 * 중앙에서 관리합니다.
 */
public class DeserializationContext {
    
    private final Map<Integer, Object> parentObjectMap;
    private final Object rootObject;
    private final JSON5Object rootJson5Object;
    private final TypeSchema rootTypeSchema;
    
    /**
     * DeserializationContext 생성자
     * 
     * @param rootObject 루트 객체
     * @param rootJson5Object 루트 JSON5Object
     * @param rootTypeSchema 루트 타입 스키마
     */
    public DeserializationContext(Object rootObject, JSON5Object rootJson5Object, TypeSchema rootTypeSchema) {
        this.parentObjectMap = new HashMap<>();
        this.rootObject = rootObject;
        this.rootJson5Object = rootJson5Object;
        this.rootTypeSchema = rootTypeSchema;
    }
    
    /**
     * 부모 객체 맵에 객체를 등록
     * 
     * @param id 스키마 ID
     * @param object 객체
     */
    public void putParentObject(Integer id, Object object) {
        parentObjectMap.put(id, object);
    }
    
    /**
     * 부모 객체 맵에서 객체를 조회
     * 
     * @param id 스키마 ID
     * @return 객체 (없으면 null)
     */
    public Object getParentObject(Integer id) {
        return parentObjectMap.get(id);
    }
    
    /**
     * 부모 객체 맵에 객체가 있는지 확인
     * 
     * @param id 스키마 ID
     * @return 존재 여부
     */
    public boolean containsParentObject(Integer id) {
        return parentObjectMap.containsKey(id);
    }
    
    /**
     * 루트 객체 반환
     * 
     * @return 루트 객체
     */
    public Object getRootObject() {
        return rootObject;
    }
    
    /**
     * 루트 JSON5Object 반환
     * 
     * @return 루트 JSON5Object
     */
    public JSON5Object getRootJson5Object() {
        return rootJson5Object;
    }
    
    /**
     * 루트 타입 스키마 반환
     * 
     * @return 루트 타입 스키마
     */
    public TypeSchema getRootTypeSchema() {
        return rootTypeSchema;
    }
    
    /**
     * 모든 부모 객체 맵 반환 (내부 사용)
     * 
     * @return 부모 객체 맵
     */
    public Map<Integer, Object> getParentObjectMap() {
        return parentObjectMap;
    }
    
    /**
     * 디버깅용 문자열 표현
     * 
     * @return 문자열 표현
     */
    @Override
    public String toString() {
        return "DeserializationContext{" +
                "parentObjectCount=" + parentObjectMap.size() +
                ", rootObjectType=" + (rootObject != null ? rootObject.getClass().getSimpleName() : "null") +
                ", rootTypeSchema=" + (rootTypeSchema != null ? rootTypeSchema.getType().getSimpleName() : "null") +
                '}';
    }
}
