package com.hancomins.cson.container.json;

import com.hancomins.cson.CSONException;
import com.hancomins.cson.ExceptionMessages;
import com.hancomins.cson.util.CharacterBuffer;
import com.hancomins.cson.util.MockBigInteger;
import com.hancomins.cson.util.NullValue;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ValueBuffer {


    private final CharacterBuffer numberBuffer;
    private final CharacterBuffer characterBuffer;



    enum DoubtMode {

        None,
        Null,
        Hexadecimal,

        True,
        False,
        String,

        ZeroStartNumber,
        Number,
        RealNumber,
        SignNumber,
        // 지수
        ExponentialNumberStart,
        ExponentialNegativeNumber,

        ExponentialNumber,






    }


    //boolean isAppearSign = false;


    boolean isSpecialChar = false;
    boolean unicodeChar = false;
    boolean unicodeExtend = false;
    int unicodeCharCount = 0;

    private boolean endString = false;


    /**
     * 제어문자를 허용할 것인지 여부를 설정한다.
     */
    private boolean allowControlChar = false;
    private boolean ignoreControlChar = false;



    private DoubtMode doubtMode = DoubtMode.None;
    private char quoteChar = '\0';


    public ValueBuffer() {
        this(new CharacterBuffer());
    }

    ValueBuffer(CharacterBuffer characterBuffer) {
        this.characterBuffer = characterBuffer;
        numberBuffer = new CharacterBuffer();

    }

    public void setAllowControlChar(boolean allowControlChar) {
        this.allowControlChar = allowControlChar;
    }
    public void setIgnoreControlChar(boolean ignoreControlChar) {
        this.ignoreControlChar = ignoreControlChar;
    }



    public void setOnlyString(char quote) {
        this.doubtMode = DoubtMode.String;
        quoteChar = quote;
    }



    public void append(String value) {
        for(int i = 0; i < value.length(); ++i) {
            append(value.charAt(i));
        }
    }



    public ValueBuffer reset() {

        characterBuffer.reset();
        doubtMode = DoubtMode.None;
        isSpecialChar = false;
        unicodeChar = false;
        unicodeExtend = false;
        unicodeCharCount = 0;
        markStartUnicodeIndex = -1;
        endString = false;
        numberBuffer.reset();
        return this;
    }





    public void append(char c) {
        switch (doubtMode) {
            case None:
                appendFirstChar(c);
                break;
            case ZeroStartNumber:
                appendZeroNumberStart(c);
                break;
            case Hexadecimal:
               appendHexadecimal(c);
                break;
            case SignNumber:
                appendSignNumber(c);
                break;
            case RealNumber:
                appendRealNumber(c);
                break;
            case Number:
                appendNumber(c);
                break;
            case ExponentialNumberStart:
                appendExponentialNumberStart(c);
                break;
            case ExponentialNegativeNumber:
                appendExponentialNumber(c, true);
                break;
            case ExponentialNumber:
                appendExponentialNumber(c, false);
                break;
            default:
                appendChar_(c);
        }
    }


    private void appendFirstChar(char c) {
        switch (c) {
            case  '+':
                doubtMode = DoubtMode.SignNumber;
                characterBuffer.append(c);
                break;
            case '-':
                doubtMode = DoubtMode.SignNumber;
                characterBuffer.append(c);
                numberBuffer.append(c);
                break;
            case '.':
                doubtMode = DoubtMode.RealNumber;
                characterBuffer.append(c);
                numberBuffer.append('0');
                numberBuffer.append('.');
                break;
            case '0':
                doubtMode = DoubtMode.ZeroStartNumber;
                characterBuffer.append(c);
                break;
            default:
                if (c >= '1' && c <= '9') {
                    doubtMode = DoubtMode.Number;
                    characterBuffer.append(c);
                    numberBuffer.append(c);
                }
                else {
                    doubtMode = DoubtMode.String;
                    appendChar_(c);
                }
        }
    }

    private void appendZeroNumberStart(char c) {
        if(c == '.') {
            characterBuffer.append(c);
            numberBuffer.append('0').append('.');
            doubtMode = DoubtMode.RealNumber;
        } else if(c == '0') {
            characterBuffer.append(c);
        }
        else if(c == 'e' || c == 'E') {
            characterBuffer.append(c);
            numberBuffer.append('0').append(c);
            doubtMode = DoubtMode.ExponentialNumberStart;
        }
        else if(c == 'x' || c == 'X') {
            doubtMode = DoubtMode.Hexadecimal;
            characterBuffer.append(c);
        }
        else if(c >= '1' && c <= '9') {
            doubtMode = DoubtMode.Number;
            characterBuffer.append(c);
            numberBuffer.append(c);
        } else {
            doubtMode = DoubtMode.String;
            appendChar_(c);
        }
    }

    private void appendHexadecimal(char c) {
        if(isHexadecimalChar(c)) {
            characterBuffer.append(c);
            numberBuffer.append(c);
        } else {
            doubtMode = DoubtMode.String;
            appendChar_(c);
        }
    }

    private void appendSignNumber(char c) {
        if(c == '.') {
            doubtMode = DoubtMode.RealNumber;
            characterBuffer.append(c);
            numberBuffer.append('0').append('.');
        } else if(c == '0') {
            doubtMode = DoubtMode.ZeroStartNumber;
            characterBuffer.append(c);
        }
        else if(c >= '1' && c <= '9') {
            doubtMode = DoubtMode.Number;
            characterBuffer.append(c);
            numberBuffer.append(c);
        } else {
            doubtMode = DoubtMode.String;
            appendChar_(c);
        }
    }

    private void appendNumber(char c) {
        if(c == '.') {
            doubtMode = DoubtMode.RealNumber;
            characterBuffer.append(c);
            numberBuffer.append(c);
        } else {
            appendRealNumber(c);
        }

    }

    private void appendRealNumber(char c) {
        if(c == 'e' || c == 'E') {
            doubtMode = DoubtMode.ExponentialNumberStart;
            characterBuffer.append(c);
            numberBuffer.append(c);
        } else if(c >= '0' && c <= '9') {
            characterBuffer.append(c);
            numberBuffer.append(c);
        } else {
            doubtMode = DoubtMode.String;
            appendChar_(c);
        }
    }

    private void appendExponentialNumberStart(char c) {
        if(c == '+') {
            doubtMode = DoubtMode.ExponentialNumber;
            characterBuffer.append(c);

        } else if(c == '-') {
            numberBuffer.append(c);
            characterBuffer.append(c);
            doubtMode = DoubtMode.ExponentialNegativeNumber;
        }
        else if(c >= '0' && c <= '9') {
            doubtMode = DoubtMode.ExponentialNumber;
            characterBuffer.append(c);
            numberBuffer.append(c);
        } else {
            doubtMode = DoubtMode.String;
            appendChar_(c);
        }
    }

    private void appendExponentialNumber(char c, boolean isNegative) {
        if(c >= '0' && c <= '9') {
            if(isNegative) {
                doubtMode = DoubtMode.ExponentialNumber;
            }
            characterBuffer.append(c);
            numberBuffer.append(c);
        } else {
            doubtMode = DoubtMode.String;
            appendChar_(c);
        }
    }

    public Object parseValue() {
        try {
            switch (doubtMode) {
                case Number:
                    return parseInteger(numberBuffer);
                case RealNumber:
                case ExponentialNumber:
                    return parseDecimal(numberBuffer);
                case Hexadecimal:
                    return parseHexadecimal(numberBuffer);
                case ZeroStartNumber:
                    return 0;
            }
            int len =  characterBuffer.length();
            String value = characterBuffer.toString();
            if(len < 10 ) {
                String lowerCaseValue = value.toLowerCase();
                switch (lowerCaseValue) {
                    case "true":
                        return Boolean.TRUE;
                    case "false":
                        return Boolean.FALSE;
                    case "null":
                        return NullValue.Instance;
                    case "nan":
                        return Double.NaN;
                    case "infinity":
                    case "+infinity":
                        return Double.POSITIVE_INFINITY;
                    case "-infinity":
                        return Double.NEGATIVE_INFINITY;
                }
            }
            return value;
        } finally {
            reset();
        }
    }

    public static Object parseDecimal(CharacterBuffer characterBuffer) {
        char[] chars = characterBuffer.getChars();
        int length = characterBuffer.length();
        return new BigDecimal(chars, 0, length);
    }


    private static Object parseHexadecimal(CharacterBuffer characterBuffer) {
        char[] chars = characterBuffer.getChars();
        int length = characterBuffer.length();
        if(length < 3) {
            throw new NumberFormatException("Invalid hexadecimal number container");
        }
        String resultString = new String(chars, 0, length);
        BigInteger bigInteger = new BigInteger(resultString, 16);
        if(bigInteger.bitLength() <= 31){
            return bigInteger.intValue();
        } else if(bigInteger.bitLength() <= 63) {
            return bigInteger.longValue();
        }
        return bigInteger;
    }

    private static Object parseInteger(CharacterBuffer characterBuffer) {
        char[] chars = characterBuffer.getChars();
        int length = characterBuffer.length();

        if(length < 18) {
            MockBigInteger mockBigInteger = new MockBigInteger(chars, 0, length);
            if(mockBigInteger.bitLength() <= 31){
                return mockBigInteger.intValue();
            } else  {
                return mockBigInteger.longValue();
            }
        }

        String resultString = new String(chars,0, length);
        BigInteger bigInteger = new BigInteger(resultString);
        if(bigInteger.bitLength() <= 63){
            return bigInteger.longValue();
        }
        return bigInteger;
    }

    boolean isEndQuote() {
        return endString;
    }



    int markStartUnicodeIndex = -1;


    private void appendChar_(char c) {
        if(c == '\\' && !isSpecialChar) {
            isSpecialChar = true;
        } else if(isSpecialChar) {
            doubtMode = DoubtMode.String;
            if(unicodeChar) {
                readUnicode(c);
                return;
            }
            isSpecialChar = false;
            switch (c) {
                case 'b':
                    characterBuffer.append('\b');
                    break;
                case 't':
                    characterBuffer.append('\t');
                    break;
                case 'n':
                    characterBuffer.append('\n');
                    break;
                case 'f':
                    characterBuffer.append('\f');
                    break;
                case 'v':
                    characterBuffer.append('\u000B');
                    break;
                case 'r':
                    characterBuffer.append('\r');
                    break;
                case 'u':
                    isSpecialChar = true;
                    unicodeChar = true;
                    unicodeCharCount = 0;
                    break;
                default:
                    //if(c != '\n' && c != '\r') {
                    characterBuffer.append(c);
                    //}
                    isSpecialChar = false;
                    break;
            }
        }

        else if(!allowControlChar && Character.isISOControl(c)) {
            if(ignoreControlChar) {
               return;
            }
            throw new CSONException(ExceptionMessages.getCtrlCharNotAllowed(c));
        }
        else {
            if(c == quoteChar) {
                endString = true;
                return;
            }

            characterBuffer.append(c);
        }
    }





    public Number parseNumber() {
        Object value = parseValue();
        if(value instanceof Number) {
            return (Number)value;
        }
        return null;
    }


    private static String hexToUnicode(String hexString ) {
        int unicode = Integer.parseInt(hexString, 16);
        return new String(Character.toChars(unicode));
    }

    private void readUnicode(char c) {
        if(c == '{') {
            unicodeExtend = true;
            markStartUnicodeIndex = characterBuffer.length();
            characterBuffer.append(c);
            return;
        } else if(markStartUnicodeIndex == -1) {
            markStartUnicodeIndex = characterBuffer.length();
        }
        ++unicodeCharCount;
        characterBuffer.append(c);
        if((unicodeCharCount == 4 && !unicodeExtend) || (unicodeExtend && c == '}')) {

            unicodeCharCount = 0;
            int end = characterBuffer.length();
            if(unicodeExtend) {
                end -= 1;
                ++markStartUnicodeIndex;
            }
            String unicode = characterBuffer.subSequence(markStartUnicodeIndex, end);
            if(unicodeExtend) {
                characterBuffer.setLength(markStartUnicodeIndex - 1);
            } else {
                characterBuffer.setLength(markStartUnicodeIndex);
            }

            characterBuffer.append(hexToUnicode(unicode));
            markStartUnicodeIndex = -1;
            isSpecialChar = false;
            unicodeChar = false;
            unicodeExtend = false;
        }
    }

    public String getStringAndReset() {
        String result = characterBuffer.toString();
        reset();
        return result;
    }


    @Override
    public String toString() {
        return characterBuffer.toString();
    }

    @SuppressWarnings("unused")
    public String toTrimString() {
        if(doubtMode == DoubtMode.Null) {
            return null;
        }
        return characterBuffer.toTrimString();
    }



    public String toString(boolean quote) {
        if(quote) {
            characterBuffer.decreaseLength(1);
        }
        return characterBuffer.toString();

    }

    private static boolean isHexadecimalChar(char c) {
        return (c >= '1' && c <= '9') || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
    }

    @SuppressWarnings("unused")
    boolean isEmpty() {
        return characterBuffer.isEmpty();
    }



}
