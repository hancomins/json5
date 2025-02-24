package com.hancomins.json5.container.json5;

enum CommentParsingState {
    None,
    Header,
    Tail,
    BeforeKey,
    AfterKey,
    BeforeValue,
    AfterValue
}
