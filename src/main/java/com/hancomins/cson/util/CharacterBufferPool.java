/*package com.hancomins.cson.util;

import java.util.concurrent.ConcurrentLinkedQueue;

public class CharacterBufferPool {


    private static final CharacterBufferPool instance = new CharacterBufferPool();

    // 버퍼 풀
    private final ConcurrentLinkedQueue<CharacterBuffer> pool = new ConcurrentLinkedQueue<>();

    public static CharacterBufferPool getInstance() {
        return instance;
    }


    // 정적 싱글턴 인스턴스
    private CharacterBufferPool() {


    }

    // 버퍼를 가져오는 메서드
    public  CharacterBuffer obtain() {
        CharacterBuffer buffer = pool.poll();
        if (buffer == null) {
            // 풀에 버퍼가 없으면 새로 생성
            buffer = new CharacterBuffer();
        }
        return buffer;
    }

    // 사용한 버퍼를 반환하는 메서드
    public  void release(CharacterBuffer buffer) {

        pool.offer(buffer);
    }

    // 현재 풀의 크기를 반환 (디버깅 용도)
    public  int getPoolSize() {
        return pool.size();
    }
}
*/