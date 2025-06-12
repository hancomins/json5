package com.hancomins.json5.serializer;

public interface OnCatchExceptionListener {
    /**
     * 예외 발생 시 호출되는 메소드입니다.
     *
     * @param e 발생한 예외
     * @return true 를 반환하면 예외를 무시하지 않고, RuntimeException으로 다시 던집니다.
     */
    boolean onException(String log, Exception e);
}
