package com.hancomins.cson.util;
import java.io.IOException;
import java.io.Reader;


public class NoSynchronizedStringReader extends Reader {

    private String str;  // 문자열 자체를 유지
    private final int length;
    private int next = 0;
    private int mark = 0;

    public NoSynchronizedStringReader(String s) {
        this.str = s;
        this.length = s.length();
    }

    private void ensureOpen() throws IOException {
        if (str == null)
            throw new IOException("Stream closed");
    }

    @Override
    public int read() throws IOException {
        ensureOpen();
        if (next >= length)
            return -1;
        return str.charAt(next++); // char[] 대신 String.charAt 사용
    }

    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
        ensureOpen();
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        if (next >= length)
            return -1;
        int n = Math.min(length - next, len);
        str.getChars(next, next + n, cbuf, off); // String.getChars 사용
        next += n;
        return n;
    }

    @Override
    public long skip(long ns) throws IOException {
        ensureOpen();
        if (next >= length)
            return 0;
        long n = Math.min(length - next, ns);
        n = Math.max(-next, n);
        next += n;
        return n;
    }

    @Override
    public boolean ready() throws IOException {
        ensureOpen();
        return true;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        ensureOpen();
        mark = next;
    }

    @Override
    public void reset() throws IOException {
        ensureOpen();
        next = mark;
    }

    @Override
    public void close() {
        str = null;
    }
}
