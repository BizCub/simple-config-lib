package com.bizcub.lib.autoconfig.util;

public final class KeyFormatter {

    public static String toSnakeCase(final String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            boolean prevLower = i > 0 && Character.isLowerCase(input.charAt(i - 1));
            boolean prevDigit = i > 0 && Character.isDigit(input.charAt(i - 1));
            if (Character.isUpperCase(c) && (prevLower || prevDigit)) {
                sb.append('_');
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }
}
