package com.hancomins.json5;

import com.hancomins.json5.options.JSON5WriterOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DisplayName("CommentParsingTest (성공)")
public class CommentParsingTest {

    @Test
    @DisplayName("quoted 키에 대한 코멘트 파싱 테스트 (성공)")
    public void parseSimpleUnquotedKeyComment() {
        String json = "{/*comment*/ //this comment\n ke/y/*23*/ : value }";
        JSON5Object json5Object = new JSON5Object(json);
        assertEquals("ke/y", json5Object.keySet().iterator().next());
        assertEquals("comment\nthis comment", json5Object.getCommentForKey("ke/y"));
        assertEquals("23", json5Object.getCommentAfterKey("ke/y"));
        System.out.println(json5Object.toString());
    }

    @Test
    @DisplayName("quoted 키에 대한 코멘트 파싱 테스트 (성공)")
    public void parseSimpleQuotedKeyComment() {
        String json = "{/*comment*/ //this comment\n 'ke/y'/*23*/ : value }";
        JSON5Object json5Object = new JSON5Object(json);
        assertEquals("ke/y", json5Object.keySet().iterator().next());
        assertEquals("comment\nthis comment", json5Object.getCommentForKey("ke/y"));
        assertEquals("23", json5Object.getCommentAfterKey("ke/y"));
        System.out.println(json5Object.toString());
    }

    @Test
    @DisplayName("값에 대한 코멘트 파싱 테스트.1 (성공)")
    public void parseSimpleValueComment1() {
        String json = "{/*comment*/ //this comment\n ke/y/*23*/ : //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  value /*   코멘트 값 값 값 */ // zzzz\n}";
        JSON5Object json5Object = new JSON5Object(json);
        assertEquals("ke/y", json5Object.keySet().iterator().next());
        assertEquals("comment\nthis comment", json5Object.getCommentForKey("ke/y"));
        assertEquals("23", json5Object.getCommentAfterKey("ke/y"));

        assertEquals("value", json5Object.get("ke/y"));

        assertEquals("코멘트1 \n 코멘트2\n\n코멘트3 ", json5Object.getCommentForValue("ke/y"));
        assertEquals("   코멘트 값 값 값 \n zzzz", json5Object.getCommentAfterValue("ke/y"));
    }

    @Test
    @DisplayName("값에 대한 코멘트 파싱 테스트.2 (성공)")
    public void parseSimpleValueComment2() {
        String json = "{/*comment*/ //this comment\n ke/y/*23*/ : //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  \"value\" /*   코멘트 값 값 값 */ // zzzz\n}";
        JSON5Object json5Object = new JSON5Object(json);
        assertEquals("ke/y", json5Object.keySet().iterator().next());
        assertEquals("comment\nthis comment", json5Object.getCommentForKey("ke/y"));
        assertEquals("23", json5Object.getCommentAfterKey("ke/y"));
        assertEquals("value", json5Object.get("ke/y"));

        assertEquals("코멘트1 \n 코멘트2\n\n코멘트3 ", json5Object.getCommentForValue("ke/y"));
        assertEquals("   코멘트 값 값 값 \n zzzz", json5Object.getCommentAfterValue("ke/y"));
    }

    @Test
    @DisplayName("값에 대한 코멘트 파싱 테스트.3 (성공)")
    public void parseSimpleValueComment3() {
        String json = "{/*comment*/ //this comment\n ke/y/*23*/ : //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  {/*이건 파싱하면 안됨*/} /*   코멘트 값 값 값 */ // zzzz\n}";
        JSON5Object json5Object = new JSON5Object(json);
        assertEquals("ke/y", json5Object.keySet().iterator().next());
        assertEquals("comment\nthis comment", json5Object.getCommentForKey("ke/y"));
        assertEquals("23", json5Object.getCommentAfterKey("ke/y"));

        assertEquals("{}", json5Object.getJSON5Object("ke/y").toString(JSON5WriterOption.json()));
        assertEquals("코멘트1 \n 코멘트2\n\n코멘트3 ", json5Object.getCommentForValue("ke/y"));
        assertEquals("   코멘트 값 값 값 \n zzzz", json5Object.getCommentAfterValue("ke/y"));
    }

    @Test
    @DisplayName("값에 대한 코멘트 파싱 테스트.4 (성공)")
    public void parseSimpleValueComment4() {
        String json = "{/*comment*/ //this comment\n ke/y/*23*/ : //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  [/*이건 파싱하면 안됨*/] /*   코멘트 값 값 값 */ // zzzz\n}";
        JSON5Object json5Object = new JSON5Object(json);
        assertEquals("ke/y", json5Object.keySet().iterator().next());
        assertEquals("comment\nthis comment", json5Object.getCommentForKey("ke/y"));
        assertEquals("23", json5Object.getCommentAfterKey("ke/y"));
        assertEquals("[]", json5Object.getJSON5Array("ke/y").toString(JSON5WriterOption.json()));
        System.out.println(json5Object.getJSON5Array("ke/y"));

        assertEquals("코멘트1 \n 코멘트2\n\n코멘트3 ", json5Object.getCommentForValue("ke/y"));
        assertEquals("   코멘트 값 값 값 \n zzzz", json5Object.getCommentAfterValue("ke/y"));
    }

    @Test
    @DisplayName("Number 값에대한 코멘트 파싱 테스트.4 (성공)")
    public void parseNumberValueComment4() {
        String json = "{/*comment*/ //this comment\n ke/y/*23*/ : //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  123123 /*   코멘트 값 값 값 */ // zzzz\n,}";
        JSON5Object json5Object = new JSON5Object(json);
        assertEquals("ke/y", json5Object.keySet().iterator().next());
        assertEquals("comment\nthis comment", json5Object.getCommentForKey("ke/y"));
        assertEquals("23", json5Object.getCommentAfterKey("ke/y"));
        assertEquals(123123, json5Object.getInt("ke/y"));
        assertEquals("코멘트1 \n 코멘트2\n\n코멘트3 ", json5Object.getCommentForValue("ke/y"));
        assertEquals("   코멘트 값 값 값 \n zzzz", json5Object.getCommentAfterValue("ke/y"));
    }

    @Test
    @DisplayName("JSON5Array 내의 코멘트 파싱 테스트.1")
    public void parseCommentInArray1() {
        String json = "[/*comment*/ //this comment\n //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  123123 /*   코멘트 값 값 값 */ // zzzz\n,]";
        JSON5Array json5Array = new JSON5Array(json);
        assertEquals("comment\nthis comment\n코멘트1 \n 코멘트2\n\n코멘트3 ", json5Array.getCommentForValue(0));
        assertEquals("   코멘트 값 값 값 \n zzzz", json5Array.getCommentAfterValue(0));
        assertEquals(123123, json5Array.getInt(0));

    }

    @Test
    @DisplayName("JSON5Array 내의 코멘트 파싱 테스트.2")
    public void parseCommentInArray2() {
        String json = "[/*comment*/ //this comment\n //코멘트1 \n /* 코멘트2\n*/ //코멘트3 \n  [] /*   코멘트 값 값 값 */ // zzzz\n,]";
        JSON5Array json5Array = new JSON5Array(json);
        assertEquals("comment\nthis comment\n코멘트1 \n 코멘트2\n\n코멘트3 ", json5Array.getCommentForValue(0));
        assertEquals("   코멘트 값 값 값 \n zzzz", json5Array.getCommentAfterValue(0));
        assertTrue(json5Array.get(0) instanceof JSON5Array);

    }
}
