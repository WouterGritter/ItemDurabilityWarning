package me.woutergritter.plugintemplate.util.string;

public class StringUtils {
    private StringUtils() {
    }

    public static String prettifyString(String s) {
        // Replace - and _ with spaces
        s = s.replace('-', ' ').replace('_', ' ');

        // toLowerCase whole string, except for the first char
        s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();

        return s;
    }

    public static String toScreamingSnakeCase(String s) {
        // Replace space and - with _
        s = s.replace(' ', '_').replace('-', '_');

        // toUpperCase whole string
        s = s.toUpperCase();

        return s;
    }
}
