package de.theholyexception.holyapi.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BiMap<K,V> implements Map<K,V> {

    Map<K, V> map = new ConcurrentHashMap<>();
    Map<V, K> inverseMap = new ConcurrentHashMap<>();


    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    public K getByValue(V value) {
        return inverseMap.get(value);
    }

    @Override
    public V put(K key, V value) {
        inverseMap.put(value, key);
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        V v;
        v = map.remove(key);
        inverseMap.remove(v);
        return v;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            inverseMap.put(entry.getValue(), entry.getKey());
        }
    }

    @Override
    public void clear() {
        map.clear();
        inverseMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}
