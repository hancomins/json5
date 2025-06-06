package com.hancomins.json5.container.json5;

import com.hancomins.json5.JSON5Path;
import com.hancomins.json5.container.JSON5ParseException;
import com.hancomins.json5.CommentPosition;
import com.hancomins.json5.ExceptionMessages;
import com.hancomins.json5.container.*;
import com.hancomins.json5.options.JsonParsingOptions;
import com.hancomins.json5.util.ArrayStack;
import com.hancomins.json5.util.CharacterBuffer;
import com.hancomins.json5.util.NullValue;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.LinkedBlockingDeque;

import static com.hancomins.json5.container.json5.ParsingState.Open;


public class JSON5Parser {

    private ParsingState parsingState = Open;
    private final ValueBuffer valueBuffer;
    private final ArrayStack<BaseDataContainer> baseDataContainerStack = new ArrayStack<>();

    private String currentKey = null;
    private CommentBuffer commentBuffer = null;
    private String comment = null;
    private CommentParsingState commentParsingState = CommentParsingState.None;

    private BaseDataContainer currentContainer = null;
    private BaseDataContainer parentContainer = null;
    private KeyValueDataContainerFactory keyValueDataContainerFactory;
    private ArrayDataContainerFactory arrayDataContainerFactory;

    private void reset() {
        parsingState = Open;
        valueBuffer.reset();
        currentKey = null;
        baseDataContainerStack.clear();
        commentBuffer = null;
        comment = null;
        commentParsingState = CommentParsingState.None;
        currentContainer = null;
        parentContainer = null;
        keyValueDataContainerFactory = null;
        arrayDataContainerFactory = null;


    }


    int line = 1;

    private final boolean allowUnquoted;
    private final boolean allowComment;



    private final boolean ignoreTrailingData;
    private final boolean skipComments;
    private ReadCountReader readCountReader;

    private JSON5Parser(JsonParsingOptions jsonOption) {
        this.allowUnquoted = jsonOption.isAllowUnquoted();
        this.allowComment = jsonOption.isAllowComments();
        this.skipComments = jsonOption.isSkipComments();


        CharacterBuffer keyBuffer = new CharacterBuffer(128);
        this.ignoreTrailingData = jsonOption.isIgnoreTrailingData();
        valueBuffer = new ValueBuffer(keyBuffer);
        valueBuffer.setIgnoreControlChar(jsonOption.isIgnoreControlCharacters());
        valueBuffer.setAllowControlChar(jsonOption.isAllowControlCharacters());
    }





    private BaseDataContainer rootContainer;

    private boolean isJSON5 = false;
    private boolean hasComment = false;
    private boolean consecutiveCommas = false;
    private String headComment = null;


    @SuppressWarnings("UnusedReturnValue")
    public static BaseDataContainer parse(Reader reader, JsonParsingOptions jsonOption, BaseDataContainer rootContainer, KeyValueDataContainerFactory keyValueDataContainerFactory, ArrayDataContainerFactory arrayDataContainerFactory) {
        JSON5Parser parser = new JSON5Parser(jsonOption);
        try {
            parser.keyValueDataContainerFactory = keyValueDataContainerFactory;
            parser.arrayDataContainerFactory = arrayDataContainerFactory;
            parser.doParse(reader, rootContainer);
        } finally {
            try {
                reader.close();
            } catch (IOException ignored) {}
            parser.reset();

        }
        return rootContainer;
    }





    private void doParse(Reader reader, BaseDataContainer rootContainer_) {


        reader = this.readCountReader  = new ReadCountReader(reader);

        this.rootContainer = rootContainer_;

        try {
            int v;



            while((v = reader.read()) != -1) {
                char c = (char)v;


                switch (parsingState) {
                    case Open:
                        inStateOpen(reader, c);
                        break;
                    case WaitKey:
                        inStateWaitKey(reader, c);
                        break;
                    case InKey:
                        if(inStateInKey(reader, c)) {
                            continue;
                        }
                        break;
                    case InKeyUnquoted:
                        if(inStateInKeyUnquoted(reader, c)) {
                            continue;
                        }
                        break;
                    case WaitKeyEndSeparator:
                        inStateWaitKeyEndSeparator(reader, c);
                        break;
                    case WaitValue:
                       inStateWaitValue(reader, c);
                        break;
                    case String:
                        if(inStateInStringValue(reader, c)) {
                            continue;
                        }
                        break;
                    case InValueUnquoted:
                        if(inStateInValueUnquoted(reader,c)) {
                            continue;
                        }
                        break;
                    case WaitNextStoreSeparatorInObject:
                        inStateWaitNextStoreSeparatorInObject(reader,c);
                        break;
                    case WaitNextStoreSeparatorInArray:
                        inStateWaitNextStoreSeparatorInArray(reader,c);
                        break;
                    case Close:
                        inStateClose(reader, c);
                        break;
                    case Comment:
                        inStateComment(c);
                        break;
                }
            }

            // todo : parsingState 가 Close 가 아닌 경우 예외 처리.

            switch (parsingState) {
                case Comment:
                    inStateComment('\0');
                    if (parsingState != ParsingState.Close) {
                        throw new JSON5ParseException("Unexpected end of stream", line, readCountReader.readCount);
                    }
                    break;
                case Close:
                    break;
                default:
                    throw new JSON5ParseException("Unexpected end of stream", line, readCountReader.readCount);
            }

        } catch (IOException e) {
            throw new JSON5ParseException(ExceptionMessages.formatMessage(ExceptionMessages.STRING_READ_ERROR), line, readCountReader.readCount, e);
        } finally {
            readCountReader = null;
            try {
                reader.close();
            } catch (IOException ignored) {}
        }


        endParse();


    }

    private void endParse() {
        isJSON5 = isJSON5 || hasComment;
        rootContainer.setSourceFormat(isJSON5 ?  (hasComment ? FormatType.JSON5_PRETTY :  FormatType.JSON5) : FormatType.JSON);
        readCountReader = null;
    }

    private void inStateClose(Reader reader, char c) throws IOException {
        if(ignoreTrailingData) {
            return;
        }
        c = skipSpace(reader, c);
        switch (c) {
            case '\0':
                return;
            case '/':
                startParsingCommentMode();
                return;
            default:
                throw new JSON5ParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN, c), line, readCountReader.readCount);
        }
    }

    private void inStateOpen(Reader reader, char c) throws IOException {
        c = skipSpace(reader, c);
        switch (c) {
            case '{':
                if(rootContainer == null) rootContainer = currentContainer = this.keyValueDataContainerFactory.create();
                else {
                    currentContainer = rootContainer;
                }
                parsingState = ParsingState.WaitKey;
                currentContainer.setComment(headComment, CommentPosition.HEADER);
                baseDataContainerStack.push(rootContainer);
                break;
            case '[':
                if(rootContainer == null) {
                    rootContainer = currentContainer = this.arrayDataContainerFactory.create();
                } else {
                    currentContainer = rootContainer;
                }
                currentContainer.setComment(headComment, CommentPosition.HEADER);
                parsingState = ParsingState.WaitValue;
                baseDataContainerStack.push(rootContainer);
                break;
            case '/':
                startParsingCommentMode();
                break;
            default:
                throw new JSON5ParseException(ExceptionMessages.formatMessage(ExceptionMessages.JSON5_BRACKET_NOT_FOUND), line, readCountReader.readCount);
        }
    }



    private boolean inStateInStringValue(Reader reader, char c) throws IOException {
        int v = c;
        do {
            c = (char)v;
            valueBuffer.append(c);
            if(valueBuffer.isEndQuote()) {
                putStringData(currentContainer, valueBuffer.getStringAndReset(), currentKey);
                currentKey = null;
                parsingState = afterValue(currentContainer);
                return true;
            }
        } while((v = reader.read()) != -1);
        return false;
    }

    private boolean inStateInKey(Reader reader, char c) throws IOException {
        int v = c;
        do {
            c = (char)v;
            valueBuffer.append(c);
            if(valueBuffer.isEndQuote()) {
                keyEnd(ParsingState.WaitKeyEndSeparator);
                return true;
            }
        } while((v = reader.read()) != -1);
        return false;
    }

    private void inStateWaitKeyEndSeparator(Reader reader, char c) throws IOException {
        c = skipSpace(reader, c);
        switch (c) {
            case ':':
                parsingState = ParsingState.WaitValue;
                break;
            case '/':
                startParsingCommentMode();
            break;
            default:
                throw new JSON5ParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN_LONG, c," : / "), line, readCountReader.readCount);

        }
    }

    private boolean inStateInKeyUnquoted(Reader reader, char c) throws IOException {
        int v = c;
        do {
            c = (char)v;
            switch (c) {
                case '\n':
                case '\t':
                case '\r':
                case ' ':
                    keyEnd(ParsingState.WaitKeyEndSeparator);
                    return true;
                case ':':
                    keyEnd(ParsingState.WaitValue);
                    return true;
                case '/':
                    startParsingCommentMode();
                    return true;
                default:
                    valueBuffer.append(c);
            }
        } while ((v = reader.read()) != -1);
        return false;
    }

    private void inStateWaitValue(Reader reader, char c) throws IOException {
        c = skipSpace(reader, c);
        switch (c) {
            case '\0':
                throw new JSON5ParseException(ExceptionMessages.formatMessage(ExceptionMessages.END_OF_STREAM), line, readCountReader.readCount);
            case '\'':
                isJSON5 = true;
            case '"':
                parsingState = ParsingState.String;
                valueBuffer.setOnlyString(c);
                break;
            case ']':
                closeBaseDataContainer();
                break;
            case '{':
                putKeyValueDataContainer();
                break;
            case '[':
                putArrayDataContainer();
                break;
            case '/':
                startParsingCommentMode();
                break;
            case ',':
                if(!allowUnquoted) {
                    throw new JSON5ParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN, c), line, readCountReader.readCount);
                } else {
                    putNullData(currentContainer, currentKey);
                    parsingState = afterComma(currentContainer);
                }
                break;
            default:
                parsingState = ParsingState.InValueUnquoted;
                c = skipSpace(reader, c);
                valueBuffer.append(c);
                break;
        }
    }


    private void inStateWaitNextStoreSeparatorInObject(Reader reader, char current) throws IOException {
        current = skipSpace(reader, current);
        switch (current) {
            case ',':
                parsingState = ParsingState.WaitKey;
                break;
            case '}':
                closeBaseDataContainer();
                break;
            case '/':
                startParsingCommentMode();
                break;
            default:
                throw new JSON5ParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN_LONG, current, ", } /") , line, readCountReader.readCount);
        }
    }

    private void inStateWaitNextStoreSeparatorInArray(Reader reader, char current) throws IOException {
        current = skipSpace(reader, current);
        switch (current) {
            case ',':
                parsingState = ParsingState.WaitValue;
                break;
            case ']':
                closeBaseDataContainer();
                break;
            case '/':
                startParsingCommentMode();
                break;
            default:
                throw new JSON5ParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN_LONG, current, ", ] /") , line, readCountReader.readCount);
        }

    }

    private boolean inStateInValueUnquoted(Reader reader, char c) throws IOException {
        int v = c;
        do {
            c = (char)v;
            switch (c) {
                case ',':
                    putValueInUnquoted(valueBuffer, currentContainer, currentKey, allowUnquoted);
                    parsingState = afterComma(currentContainer);
                    currentKey = null;
                    return true;
                case ' ':
                case '\r':
                case '\n':
                case '\t':
                    putValueInUnquoted(valueBuffer, currentContainer, currentKey, allowUnquoted);
                    parsingState = afterValue(currentContainer);
                    return true;
                case '}':
                case ']':
                    putValueInUnquoted(valueBuffer, currentContainer, currentKey, allowUnquoted);
                    closeBaseDataContainer();
                    return true;
                case '/':
                    startParsingCommentMode();
                    return true;
                default:
                    valueBuffer.append(c);
                    break;
            }
        } while ((v = reader.read()) != -1);
        return false;
    }

    private void inStateWaitKey(Reader reader, char current) throws IOException {
        current = skipSpace(reader, current);
        switch (current) {
            case '\'':
                isJSON5 = true;
            case '"':
                parsingState = ParsingState.InKey;
                valueBuffer.setOnlyString(current);
                break;
            case '}':
                closeBaseDataContainer();
                break;
            case '/': // 코멘트 파싱 모드.
                startParsingCommentMode();
                break;
            case ',':
                if(!consecutiveCommas) {
                    throw new JSON5ParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN, current), line, readCountReader.readCount);
                }
                break;
            default:
                if(allowUnquoted) {
                    isJSON5 = true;
                    parsingState = ParsingState.InKeyUnquoted;
                    valueBuffer.append(current);
                } else {
                    throw new JSON5ParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN, current) , line, readCountReader.readCount);
                }
                break;
        }
    }



    private void inStateComment(char current) {
        if(commentBuffer == null) {
            // 코멘트를 지원하지 않는 예외.
            throw new JSON5ParseException("Invalid JSON5 document", line, readCountReader.readCount);
        }
        CommentBuffer.AppendResult result = commentBuffer.append(current);
        ParsingState lastParsingState = null;
        switch (result) {
            case Fail:
                parsingState = commentBuffer.lastParsingState();
                // 주석 상태가 유효하지 않다면, 주석 상태를 끝내고 다음 상태로 전환한다.
                if(parsingState == ParsingState.InValueUnquoted || parsingState == ParsingState.InKeyUnquoted) {
                    valueBuffer.append('/');
                    valueBuffer.append(current);
                    return;
                } else {
                    throw new JSON5ParseException(ExceptionMessages.formatMessage(ExceptionMessages.UNEXPECTED_TOKEN, current) , line, readCountReader.readCount);
                }
            case InComment:
                lastParsingState = commentBuffer.lastParsingState();
                switch (lastParsingState) {
                    case InValueUnquoted:
                        //case Number:
                        // todo: currentKey 가 null 되어도 괜찮은지 check.
                        putValueInUnquoted(valueBuffer, currentContainer, currentKey, allowUnquoted);
                        commentBuffer.changeLastParsingState(afterValue(currentContainer));
                        break;
                    case InKeyUnquoted:
                        currentKey = valueBuffer.getStringAndReset();
                        commentBuffer.changeLastParsingState(ParsingState.WaitKeyEndSeparator);
                        if(commentParsingState == CommentParsingState.BeforeKey) {
                            if(comment != null) {
                                appendBeforeKeyComment( ((KeyValueDataContainer)currentContainer));
                            }
                            commentParsingState = CommentParsingState.None;
                        }
                        break;
                }

                break;
            case End:
                parsingState = commentBuffer.lastParsingState();
                CommentParsingState oldCommentParsingState = commentParsingState;
                commentParsingState = commentBuffer.commentParsingState();
                String newComment = commentBuffer.getComment();
                if(newComment.isEmpty()) {
                    commentParsingState = CommentParsingState.None;
                    comment = "";
                }
                else if(oldCommentParsingState == commentParsingState) {
                    comment += "\n" + newComment;
                } else {
                    comment = newComment;
                }
                if(skipComments) {
                    comment = null;
                }
                else if(currentContainer instanceof KeyValueDataContainer) {
                    setCommentToKeyValueDataContainer();
                } else if(currentContainer instanceof ArrayDataContainer) {
                    setCommentToArrayDataContainer();
                }  else {
                    headComment = comment;
                }

                break;
        }
    }

    private void setCommentToKeyValueDataContainer() {
        KeyValueDataContainer json5Object = (KeyValueDataContainer)currentContainer;
        switch (commentParsingState) {
            case BeforeKey:
                json5Object.setComment(currentKey, comment, CommentPosition.BEFORE_KEY);
                break;
            case AfterKey:
                json5Object.setComment(currentKey, comment, CommentPosition.AFTER_KEY);
                break;
            case BeforeValue:
                json5Object.setComment(currentKey, comment, CommentPosition.BEFORE_VALUE);
                break;
            case AfterValue:
                String lastKey = json5Object.getLastAccessedKey();
                json5Object.setComment(lastKey, comment, CommentPosition.AFTER_VALUE);
                break;
            case Tail:
                json5Object.setComment(comment, CommentPosition.FOOTER);
                break;
        }
    }

    private void setCommentToArrayDataContainer() {
        ArrayDataContainer json5Array = (ArrayDataContainer)currentContainer;
        int size = json5Array.size();
        switch (commentParsingState) {
            case BeforeValue:
                json5Array.setComment(size, comment, CommentPosition.BEFORE_VALUE);
                break;
            case AfterValue:
                json5Array.setComment(size - 1, comment, CommentPosition.AFTER_VALUE);
                break;
            case Tail:
                json5Array.setComment(comment, CommentPosition.FOOTER);
                break;
        }
    }






    private void closeBaseDataContainer() {
        commentParsingState = CommentParsingState.None;
        currentContainer.end();
        if(currentContainer == rootContainer) {
            parsingState = ParsingState.Close;
        } else {
            currentContainer = popContainer(baseDataContainerStack);
            parsingState = afterValue(currentContainer);
        }
    }


    private void keyEnd(ParsingState parsingState) {
        this.parsingState = parsingState;
        currentKey = valueBuffer.getStringAndReset();
        if(commentParsingState == CommentParsingState.BeforeKey) {
            if(comment != null) {
                appendBeforeKeyComment( ((KeyValueDataContainer)currentContainer));
            }
            commentParsingState = CommentParsingState.None;
        }
    }

    private void appendBeforeKeyComment(KeyValueDataContainer keyValueDataContainer) {
        String alreadyComment = keyValueDataContainer.getComment(currentKey, CommentPosition.BEFORE_KEY);
        if (alreadyComment != null) {
            comment = alreadyComment + "\n" + comment;
        }
        keyValueDataContainer.setComment(currentKey, comment, CommentPosition.BEFORE_KEY);;
    }



    private void startParsingCommentMode() {
        if(commentBuffer == null) {
            if(!allowComment) {
                throw new JSON5ParseException(ExceptionMessages.formatMessage(ExceptionMessages.NOT_ALLOWED_COMMENT), line, readCountReader.readCount);
            }
            hasComment = true;
            commentBuffer = new CommentBuffer();
        }
        parsingState = startParsingCommentMode(commentBuffer, parsingState);
    }

    private void putKeyValueDataContainer() {
        commentParsingState = CommentParsingState.None;
        parentContainer = currentContainer;
        currentContainer = keyValueDataContainerFactory.create();
        baseDataContainerStack.push(currentContainer);
        putContainerData(parentContainer, currentContainer, currentKey);
        parsingState = ParsingState.WaitKey;
        currentKey = null;
    }


    private void putArrayDataContainer() {
        commentParsingState = CommentParsingState.None;
        parentContainer = currentContainer;
        currentContainer = arrayDataContainerFactory.create();
        baseDataContainerStack.push(currentContainer);
        putContainerData(parentContainer, currentContainer, currentKey);
        currentKey = null;
    }


    private ParsingState startParsingCommentMode(CommentBuffer commentBuffer, ParsingState currentParsingState) {
        if(commentBuffer == null) {
            throw new JSON5ParseException("Comment unsupported", line, readCountReader.readCount);
        }
        return commentBuffer.start(currentParsingState);
    }


    private static BaseDataContainer popContainer(ArrayStack<BaseDataContainer> BaseDataContainers) {
        BaseDataContainers.pop();
        return BaseDataContainers.peek();
    }



    private char skipSpace(Reader reader, char current) throws IOException {
        if(checkSpace(current) != '\0') {
            return current;
        }
        int v;
        while((v = reader.read()) != -1) {
            char c = (char)v;
            if(checkSpace(c) != '\0') {
                return c;
            }
        }
        return '\0';
    }


    private char checkSpace(char current) throws IOException {
        switch (current) {
            case '\n':
                readCountReader.readCount = 0;
                line++;
                break;
            case '\r':
            case '\t':
            case ' ':
                break;
            default:
                return current;
        }
        return '\0';
    }


    private static ParsingState afterValue(BaseDataContainer element) {
        return element instanceof ArrayDataContainer ? ParsingState.WaitNextStoreSeparatorInArray : ParsingState.WaitNextStoreSeparatorInObject;
    }


    private static ParsingState afterComma(BaseDataContainer element) {
        if(element instanceof ArrayDataContainer) {
            return ParsingState.WaitValue;
        } else {
            return ParsingState.WaitKey;
        }
    }

    /**
     * 값을 넣는다.
     * @param valueBuffer 값을 파싱하는 ValueBuffer
     * @param currentContainer 현재 Container
     * @param key 키
     * @param allowUnquoted unquoted 허용 여부

     */
    private void putValueInUnquoted(ValueBuffer valueBuffer, BaseDataContainer currentContainer, String key, boolean allowUnquoted) {
        Object inValue = valueBuffer.parseValue();
        if(inValue instanceof String) {
            isJSON5 = true;
            if(!allowUnquoted) {
                throw new JSON5ParseException(ExceptionMessages.formatMessage(ExceptionMessages.NOT_ALLOWED_UNQUOTED_STRING), line, readCountReader.readCount);
            }
        }
        if(currentContainer instanceof KeyValueDataContainer) {
            ((KeyValueDataContainer)currentContainer).put(key, inValue);
        } else {
            ((ArrayDataContainer)currentContainer).add(inValue);
        }
        currentKey = null;

    }




    private static void putStringData(BaseDataContainer currentContainer, String value, String key) {

        if(key != null) {
            ((KeyValueDataContainer)currentContainer).put(key, value);
        } else {
            ((ArrayDataContainer) currentContainer).add(value);
        }
    }

    private static void putNullData(BaseDataContainer currentContainer, String key) {
        if(key != null) {
            ((KeyValueDataContainer)currentContainer).put(key, NullValue.Instance);
        } else {
            ((ArrayDataContainer)currentContainer).add(NullValue.Instance);
        }
    }



    private static void putContainerData(BaseDataContainer currentContainer, BaseDataContainer value, String key) {
        if(key != null) {
            ((KeyValueDataContainer)currentContainer).put(key, value);
        } else {
            ((ArrayDataContainer)currentContainer).add(value);
        }
    }




    private static class ReadCountReader extends Reader {

        private final Reader reader;
        private int readCount = 0;


        public ReadCountReader(Reader reader) {
            this.reader = reader;
        }


        @Override
        public int read() throws IOException {
            ++readCount;
            return this.reader.read();
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            int read = this.reader.read(cbuf, off, len);
            readCount += read;
            return read;
        }


        @Override
        public void close() throws IOException {
            this.reader.close();
        }
    }

}
