package com.oycm.http.jdk;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author ouyangcm
 * create 2024/11/29 14:58
 */
public class CheckUtils {

    /**
     * 校验obj不为null
     */
    public static <T> T checkNotNull(T obj, String errorMessageTemplate, Object... errorMessageArgs) {
        if (obj == null) {
            throw new NullPointerException(format(errorMessageTemplate, errorMessageArgs));
        }

        return obj;
    }

    public static String format(String messageTemplate, Object... messageArgs) {
        return String.format(messageTemplate, messageArgs);
    }

    public static <T> Collection<T> valuesOrEmpty(Map<String, Collection<T>> map, String key) {

        Collection<T> values = map.get(key);
        if (values != null) return values;

        return Collections.emptyList();
    }

    public static void checkState(boolean expression,
                                  String errorMessageTemplate,
                                  Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalStateException(format(errorMessageTemplate, errorMessageArgs));
        }
    }


}
