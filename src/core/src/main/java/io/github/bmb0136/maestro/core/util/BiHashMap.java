package io.github.bmb0136.maestro.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BiHashMap<T1, T2> {
    private final HashMap<T1, T2> forward = new HashMap<>();
    private final HashMap<T2, T1> reverse = new HashMap<>();

    public int size() {
        return forward.size();
    }

    public boolean isEmpty() {
        return forward.isEmpty();
    }

    public boolean add(T1 v1, T2 v2) {
        if (forward.containsKey(v1)) {
            return false;
        }
        if (reverse.containsKey(v2)) {
            return false;
        }
        forward.put(v1, v2);
        reverse.put(v2, v1);
        return true;
    }

    public boolean remove1(T1 key1) {
        var key2 = forward.remove(key1);
        if (key2 != null) {
            reverse.remove(key2);
            return true;
        }
        return false;
    }

    public boolean remove2(T2 key2) {
        var key1 = reverse.remove(key2);
        if (key1 != null) {
            forward.remove(key1);
            return true;
        }
        return false;
    }

    public boolean contains1(T1 v1) {
        return forward.containsKey(v1);
    }

    public boolean contains2(T2 v2) {
        return reverse.containsKey(v2);
    }

    public T2 get1(T1 v1) {
        return forward.get(v1);
    }

    public T1 get2(T2 v2) {
        return reverse.get(v2);
    }

    public Set<T1> values1() {
        return forward.keySet();
    }

    public Set<T2> values2() {
        return reverse.keySet();
    }

    public Set<Map.Entry<T1, T2>> entries1() {
        return forward.entrySet();
    }

    public Set<Map.Entry<T2, T1>> entries2() {
        return reverse.entrySet();
    }
}
