package net.exemine.api.util;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class MultiValueMap<K, V> {

    private final Map<K, List<V>> map;
    private final Supplier<List<V>> listSupplier;

    public MultiValueMap(Map<K, List<V>> map) {
        this(map, ArrayList::new);
    }

    public MultiValueMap(Supplier<List<V>> listSupplier) {
        this(new HashMap<>(), listSupplier);
    }

    public MultiValueMap() {
        this(new HashMap<>(), ArrayList::new);
    }

    public void put(K key, V value) {
        map.computeIfAbsent(key, k -> listSupplier.get()).add(value);
    }

    public void remove(K key, V value) {
        List<V> values = map.get(key);
        if (values == null) return;
        values.remove(value);
        if (values.isEmpty()) {
            map.remove(key);
        }
    }

    public List<V> get(K key) {
        return map.getOrDefault(key, listSupplier.get());
    }
}
