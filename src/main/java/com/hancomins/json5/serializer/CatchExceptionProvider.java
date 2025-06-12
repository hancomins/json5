package com.hancomins.json5.serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CatchExceptionProvider {
    private static final CatchExceptionProvider instance = new CatchExceptionProvider();
    private final List<OnCatchExceptionListener> onCatchExceptionList = new CopyOnWriteArrayList<>();

    private CatchExceptionProvider() {
        // 기본 생성자
    }

    public static CatchExceptionProvider getInstance() {
        return instance;
    };

    /**
     * 예외를 처리할 핸들러를 추가합니다.
     *
     * @param onCatchException 예외 처리 핸들러
     */
    public void addOnCatchException(OnCatchExceptionListener onCatchException) {
        if (onCatchException != null) {
            onCatchExceptionList.add(onCatchException);
        }
    }

    /**
     * 등록된 모든 예외 처리 핸들러를 호출합니다.
     *
     * @param e 발생한 예외
     */
    public void catchException(String log, Exception e) {
        if (e == null) return;

        List<OnCatchExceptionListener> handlers = new ArrayList<>(onCatchExceptionList);
        if(handlers.isEmpty()) {
            System.err.println(log);
            e.printStackTrace(System.err);
            return;
        }

        boolean thown = false;
        for (OnCatchExceptionListener handler : handlers) {
            try {
                thown = handler.onException(log,e);
                if(thown) {
                    break;
                }
            } catch (Exception handlerException) {
                // 핸들러에서 예외가 발생해도 무시
            }
        }
        if(thown) {
            throw new RuntimeException(log, e);
        }
        // 예외를 다시 던지지 않음
    }



}
