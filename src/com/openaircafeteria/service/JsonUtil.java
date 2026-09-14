package com.openaircafeteria.service;

import java.util.List;
import java.util.Map;

/**
 * A deliberately small JSON reader/writer for this exam project.
 * It supports the object, array, string, number, boolean and null values used
 * by the cafeteria API, so the project does not need an external dependency.
 */
public final class JsonUtil {
    private JsonUtil() {
    }

    public static String stringify(Object value) {
        StringBuilder output = new StringBuilder();
        writeValue(value, output);
        return output.toString();
    }

    @SuppressWarnings("unchecked")
    private static void writeValue(Object value, StringBuilder output) {
        if (value == null) {
            output.append("null");
        } else if (value instanceof String || value instanceof Character) {
            writeString(String.valueOf(value), output);
        } else if (value instanceof Number || value instanceof Boolean) {
            output.append(String.valueOf(value));
        } else if (value instanceof Map) {
            output.append('{');
            boolean first = true;
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                if (!first) {
                    output.append(',');
                }
                writeString(String.valueOf(entry.getKey()), output);
                output.append(':');
                writeValue(entry.getValue(), output);
                first = false;
            }
            output.append('}');
        } else if (value instanceof List) {
            output.append('[');
            boolean first = true;
            for (Object item : (List<Object>) value) {
                if (!first) {
                    output.append(',');
                }
                writeValue(item, output);
                first = false;
            }
            output.append(']');
        } else {
            writeString(String.valueOf(value), output);
        }
    }

    private static void writeString(String value, StringBuilder output) {
        output.append('"');
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"':
                    output.append("\\\"");
                    break;
                case '\\':
                    output.append("\\\\");
                    break;
                case '\b':
                    output.append("\\b");
                    break;
                case '\f':
                    output.append("\\f");
                    break;
                case '\n':
                    output.append("\\n");
                    break;
                case '\r':
                    output.append("\\r");
                    break;
                case '\t':
                    output.append("\\t");
                    break;
                default:
                    if (character < 32) {
                        String hex = Integer.toHexString(character);
                        output.append("\\u");
                        for (int padding = hex.length(); padding < 4; padding++) {
                            output.append('0');
                        }
                        output.append(hex);
                    } else {
                        output.append(character);
                    }
                    break;
            }
        }
        output.append('"');
    }

    public static Object parse(String json) {
        if (json == null) {
            throw new IllegalArgumentException("Request body is empty");
        }
        Parser parser = new Parser(json);
        Object value = parser.parseValue();
        parser.skipWhitespace();
        if (!parser.isAtEnd()) {
            throw new IllegalArgumentException("Unexpected characters after JSON value");
        }
        return value;
    }

    private static final class Parser {
        private final String input;
        private int position;

        private Parser(String input) {
            this.input = input;
        }

        private Object parseValue() {
            skipWhitespace();
            if (isAtEnd()) {
                throw error("Expected a JSON value");
            }

            char current = input.charAt(position);
            if (current == '{') {
                return parseObject();
            }
            if (current == '[') {
                return parseArray();
            }
            if (current == '"') {
                return parseString();
            }
            if (input.startsWith("true", position)) {
                position += 4;
                return Boolean.TRUE;
            }
            if (input.startsWith("false", position)) {
                position += 5;
                return Boolean.FALSE;
            }
            if (input.startsWith("null", position)) {
                position += 4;
                return null;
            }
            if (current == '-' || (current >= '0' && current <= '9')) {
                return parseNumber();
            }
            throw error("Unexpected character '" + current + "'");
        }

        private Map<String, Object> parseObject() {
            java.util.LinkedHashMap<String, Object> object = new java.util.LinkedHashMap<String, Object>();
            expect('{');
            skipWhitespace();
            if (takeIf('}')) {
                return object;
            }

            while (true) {
                skipWhitespace();
                if (isAtEnd() || input.charAt(position) != '"') {
                    throw error("Object keys must be strings");
                }
                String key = parseString();
                skipWhitespace();
                expect(':');
                object.put(key, parseValue());
                skipWhitespace();
                if (takeIf('}')) {
                    return object;
                }
                expect(',');
            }
        }

        private List<Object> parseArray() {
            java.util.ArrayList<Object> array = new java.util.ArrayList<Object>();
            expect('[');
            skipWhitespace();
            if (takeIf(']')) {
                return array;
            }

            while (true) {
                array.add(parseValue());
                skipWhitespace();
                if (takeIf(']')) {
                    return array;
                }
                expect(',');
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (!isAtEnd()) {
                char current = input.charAt(position++);
                if (current == '"') {
                    return result.toString();
                }
                if (current != '\\') {
                    result.append(current);
                    continue;
                }
                if (isAtEnd()) {
                    throw error("Unfinished escape sequence");
                }
                char escaped = input.charAt(position++);
                switch (escaped) {
                    case '"':
                        result.append('"');
                        break;
                    case '\\':
                        result.append('\\');
                        break;
                    case '/':
                        result.append('/');
                        break;
                    case 'b':
                        result.append('\b');
                        break;
                    case 'f':
                        result.append('\f');
                        break;
                    case 'n':
                        result.append('\n');
                        break;
                    case 'r':
                        result.append('\r');
                        break;
                    case 't':
                        result.append('\t');
                        break;
                    case 'u':
                        result.append(parseUnicodeCharacter());
                        break;
                    default:
                        throw error("Invalid escape sequence");
                }
            }
            throw error("Unclosed string");
        }

        private char parseUnicodeCharacter() {
            if (position + 4 > input.length()) {
                throw error("Invalid unicode escape");
            }
            String hex = input.substring(position, position + 4);
            position += 4;
            try {
                return (char) Integer.parseInt(hex, 16);
            } catch (NumberFormatException exception) {
                throw error("Invalid unicode escape");
            }
        }

        private Number parseNumber() {
            int start = position;
            if (takeIf('-')) {
                // The digits are checked below.
            }
            if (takeIf('0')) {
                // A zero is a valid first digit.
            } else {
                requireDigit("Expected a number");
                while (!isAtEnd() && isDigit(input.charAt(position))) {
                    position++;
                }
            }
            boolean decimal = false;
            if (takeIf('.')) {
                decimal = true;
                requireDigit("Expected digits after decimal point");
                while (!isAtEnd() && isDigit(input.charAt(position))) {
                    position++;
                }
            }
            if (!isAtEnd() && (input.charAt(position) == 'e' || input.charAt(position) == 'E')) {
                decimal = true;
                position++;
                if (!isAtEnd() && (input.charAt(position) == '+' || input.charAt(position) == '-')) {
                    position++;
                }
                requireDigit("Expected exponent digits");
                while (!isAtEnd() && isDigit(input.charAt(position))) {
                    position++;
                }
            }
            String number = input.substring(start, position);
            try {
                if (decimal) {
                    return Double.valueOf(number);
                }
                return Long.valueOf(number);
            } catch (NumberFormatException exception) {
                throw error("Invalid number");
            }
        }

        private void requireDigit(String message) {
            if (isAtEnd() || !isDigit(input.charAt(position))) {
                throw error(message);
            }
            position++;
        }

        private boolean isDigit(char character) {
            return character >= '0' && character <= '9';
        }

        private void skipWhitespace() {
            while (!isAtEnd()) {
                char current = input.charAt(position);
                if (current == ' ' || current == '\n' || current == '\r' || current == '\t') {
                    position++;
                } else {
                    break;
                }
            }
        }

        private boolean takeIf(char expected) {
            if (!isAtEnd() && input.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        private void expect(char expected) {
            if (!takeIf(expected)) {
                throw error("Expected '" + expected + "'");
            }
        }

        private boolean isAtEnd() {
            return position >= input.length();
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at character " + position);
        }
    }
}
