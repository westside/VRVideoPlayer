package com.bhaptics.common;

import java.util.Map;

/**
 * Created by westside on 2016-04-26.
 */
public class MapUtils {
    public static <K> Boolean getBoolean(final Map<? super K, ?> map, final K key) {
        if (map != null) {
            final Object answer = map.get(key);
            if (answer != null) {
                if (answer instanceof Boolean) {
                    return (Boolean) answer;
                }
                if (answer instanceof String) {
                    return Boolean.valueOf((String) answer);
                }
                if (answer instanceof Number) {
                    final Number n = (Number) answer;
                    return n.intValue() != 0 ? Boolean.TRUE : Boolean.FALSE;
                }
            }
        }
        return null;
    }

    public static <K> Boolean getBoolean(final Map<? super K, ?> map, final K key, final Boolean defaultValue) {
        Boolean answer = getBoolean(map, key);
        if (answer == null) {
            answer = defaultValue;
        }
        return answer;
    }
}
