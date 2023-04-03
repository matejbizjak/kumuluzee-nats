package com.kumuluz.ee.nats.jetstream.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom implementation of two-key generic table.
 *
 * @author Matej Bizjak
 */

public class TwoKeyTable<K1, K2, V> {

    private final Map<K1, Map<K2, V>> table;

    public TwoKeyTable() {
        this.table = new HashMap<>();
    }

    public void put(K1 key1, K2 key2, V value) {
        if (!table.containsKey(key1)) {
            table.put(key1, new HashMap<>());
        }
        table.get(key1).put(key2, value);
    }

    public V get(K1 key1, K2 key2) {
        if (!table.containsKey(key1)) {
            return null;
        }
        return table.get(key1).get(key2);
    }

    public boolean contains(K1 key1, K2 key2) {
        if (!table.containsKey(key1)) {
            return false;
        }
        return table.get(key1).containsKey(key2);
    }
}
