package net.biville.florent.repl;

import java.util.Map;

import static org.assertj.core.util.Maps.newHashMap;

public class Maps {

    private Maps() {
        throw new RuntimeException("static");
    }

    public static <K, V> Map<K, V> mutableMap(K k1, V v1, K k2, V v2) {
        Map<K, V> result = mutableMap(k1, v1);
        result.put(k2, v2);
        return result;
    }

    public static <K, V> Map<K, V> mutableMap(K k1, V v1) {
        return newHashMap(k1, v1);
    }
}
