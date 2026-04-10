package io.github.bmb0136.maestro.core.util;

public final class StringUtils {
    private StringUtils() {}

    /**
     * Converts UPPER_SNAKE_CASE to Title Case
     * @param input The UPPER_SNAKE_CASE string
     * @return The Title Case string
     */
    public static String upperSnakeCaseToTitleCase(String input) {
        if (input == null) {
            return null;
        }

        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0 || chars[i - 1] == ' ') {
                chars[i] = Character.toUpperCase(chars[i]);
            } else if (chars[i] == '_') {
                chars[i] = ' ';
            } else {
                chars[i] = Character.toLowerCase(chars[i]);
            }
        }
        return new String(chars);
    }
}
