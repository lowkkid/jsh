package com.github.lowkkid.jsh.utils;

public final class StringUtils {

    private StringUtils() {}

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
