package com.hancomins.json5.issue;


import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.options.ParsingOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * json5 에서 '' 으로 감싸져있는 빈 문자열을 읽으면 숫자 0 으로 인식
 * https://github.com/clipsoft-rnd/json5/issues/2
 */
public class Issues2 {

    @Test
    public void test() {
        JSON5Object json5 = new JSON5Object("{a: '', b: ['']}", ParsingOptions.json5());
        System.out.println(json5);
        assertEquals("", json5.get("a").toString());
        assertEquals("", json5.getJSON5Array("b").getString(0));

    }

}
