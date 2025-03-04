package de.theholyexception.holyapi.datastorage.json;

import org.json.simple.JSONObject;

public class JSONFormatter {

    private JSONFormatter() {}

    public static String format(JSONObject json) {
        return format(json.toJSONString(), 2);
    }

    public static String format(JSONObject json, int indentation) {
        return format(json.toJSONString(), indentation);
    }

    public static String format(String json) {
        return format(json, 2);
    }

    public static String format(String json, int indentation) {
        if (json == null || json.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int currentIndentation = 0;
        boolean inString = false;
        boolean escaped = false;

        // Remove existing whitespace outside of strings
        StringBuilder compactJson = new StringBuilder();
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                compactJson.append(c);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
            } else {
                if (c == '"') {
                    inString = true;
                    compactJson.append(c);
                } else if (!Character.isWhitespace(c)) {
                    compactJson.append(c);
                }
            }
        }

        // Reset for formatting pass
        inString = false;
        escaped = false;
        String compact = compactJson.toString();

        for (int i = 0; i < compact.length(); i++) {
            char c = compact.charAt(i);

            if (inString) {
                result.append(c);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
            } else {
                switch (c) {
                    case '{':
                    case '[':
                        result.append(c);
                        currentIndentation += indentation;
                        result.append('\n');
                        appendSpaces(result, currentIndentation);
                        break;

                    case '}':
                    case ']':
                        result.append('\n');
                        currentIndentation -= indentation;
                        appendSpaces(result, currentIndentation);
                        result.append(c);
                        break;

                    case ',':
                        result.append(c);
                        result.append('\n');
                        appendSpaces(result, currentIndentation);
                        break;

                    case ':':
                        result.append(c).append(' ');
                        break;

                    case '"':
                        inString = true;
                        result.append(c);
                        break;

                    default:
                        result.append(c);
                        break;
                }
            }
        }

        return result.toString();
    }

    private static void appendSpaces(StringBuilder sb, int count) {
        for (int i = 0; i < count; i++) {
            sb.append(' ');
        }
    }


}