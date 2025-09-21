package com.solicare.app.backend.global.utils;

import java.util.Set;

public class StringUtils {
    public static String packSetString(Set<String> items) {
        return items == null || items.isEmpty() ? null : String.join(",", items);
    }

    public static Set<String> unpackSetString(String str) {
        if (str == null || str.isEmpty()) return Set.of();
        return Set.of(str.split(","));
    }
}
