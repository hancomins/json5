package com.hancomins.json5.serializer;

import java.util.ArrayList;
import java.util.List;

/**
 * ValueProcessor 인스턴스를 생성하고 관리하는 팩토리 클래스입니다.
 * 
 * <p>이 클래스는 싱글톤 패턴을 사용하여 ValueProcessor들을 효율적으로 관리하며,
 * 주어진 타입에 적합한 프로세서를 찾아 반환합니다.</p>
 * 
 * <h3>지원하는 프로세서:</h3>
 * <ul>
 *   <li>{@link PrimitiveValueProcessor} - 기본 타입 및 단순 타입</li>
 *   <li>{@link ObjectValueProcessor} - 복합 객체 타입</li>
 *   <li>{@link CollectionValueProcessor} - 컬렉션 타입</li>
 * </ul>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class ValueProcessorFactory {
    
    private static final ValueProcessorFactory INSTANCE = new ValueProcessorFactory();
    
    private final List<ValueProcessor> processors;
    private final ValueProcessor defaultProcessor;
    
    private ValueProcessorFactory() {
        processors = new ArrayList<>();
        processors.add(new PrimitiveValueProcessor());
        processors.add(new CollectionValueProcessor());
        processors.add(new ObjectValueProcessor());
        
        // 기본 프로세서는 ObjectValueProcessor 사용
        defaultProcessor = new ObjectValueProcessor();
    }
    
    /**
     * ValueProcessorFactory의 싱글톤 인스턴스를 반환합니다.
     * 
     * @return ValueProcessorFactory 인스턴스
     */
    public static ValueProcessorFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * 주어진 타입에 적합한 ValueProcessor를 반환합니다.
     * 
     * <p>등록된 프로세서들을 순서대로 확인하여 첫 번째로 처리 가능한
     * 프로세서를 반환합니다. 적합한 프로세서가 없으면 기본 프로세서를
     * 반환합니다.</p>
     * 
     * @param type 처리할 타입
     * @return 적합한 ValueProcessor
     */
    public ValueProcessor getProcessor(Types type) {
        for (ValueProcessor processor : processors) {
            if (processor.canHandle(type)) {
                return processor;
            }
        }
        return defaultProcessor;
    }
    
    /**
     * 커스텀 ValueProcessor를 등록합니다.
     * 
     * <p>새로 등록된 프로세서는 기존 프로세서들보다 우선순위를 가집니다.</p>
     * 
     * @param processor 등록할 ValueProcessor
     */
    public void registerProcessor(ValueProcessor processor) {
        // 새 프로세서를 맨 앞에 추가하여 우선순위 부여
        processors.add(0, processor);
    }
    
    /**
     * 등록된 모든 프로세서를 제거하고 기본 프로세서들만 남깁니다.
     */
    public void resetProcessors() {
        processors.clear();
        processors.add(new PrimitiveValueProcessor());
        processors.add(new CollectionValueProcessor());
        processors.add(new ObjectValueProcessor());
    }
    
    /**
     * 현재 등록된 프로세서의 개수를 반환합니다.
     * 
     * @return 등록된 프로세서 개수
     */
    public int getProcessorCount() {
        return processors.size();
    }
}
