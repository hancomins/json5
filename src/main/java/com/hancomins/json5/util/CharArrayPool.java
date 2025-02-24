package com.hancomins.json5.util;

import java.util.concurrent.ConcurrentLinkedQueue;

public class CharArrayPool {

    // 고정된 char[] 크기
    public static final int BUFFER_SIZE = 32;

    private static final CharArrayPool instance = new CharArrayPool();

    // 버퍼 풀
    private final ConcurrentLinkedQueue<char[]> pool = new ConcurrentLinkedQueue<>();

    public static CharArrayPool getInstance() {
        return instance;
    }


    // 정적 싱글턴 인스턴스
    private CharArrayPool() {

    }

    // 버퍼를 가져오는 메서드
    public  char[] obtain() {
        char[] buffer = pool.poll();
        if (buffer == null) {
            // 풀에 버퍼가 없으면 새로 생성
            buffer = new char[BUFFER_SIZE];
        }
        return buffer;
    }

    // 사용한 버퍼를 반환하는 메서드
    public  void release(char[] buffer) {
        if (buffer == null || buffer.length != BUFFER_SIZE) {
            // 버퍼가 null이거나 크기가 다르면 반환하지 않음
            return;
        }
        pool.offer(buffer);
    }

    // 현재 풀의 크기를 반환 (디버깅 용도)
    public  int getPoolSize() {
        return pool.size();
    }
}
