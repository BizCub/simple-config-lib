package io.github.bizcub.lib.util;

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

    public static String humanize(final String raw) {
        String spaced = raw
                .replace('_', ' ')
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("([A-Za-z])([0-9])", "$1 $2")
                .trim();
        StringBuilder sb = new StringBuilder(spaced.length());
        boolean capNext = true;
        for (int i = 0; i < spaced.length(); i++) {
            char c = spaced.charAt(i);
            if (c == ' ') {
                capNext = true;
                sb.append(c);
            } else if (capNext) {
                sb.append(Character.toUpperCase(c));
                capNext = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
