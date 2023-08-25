package net.exemine.api.util;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.Map;

public class CollectionUtil {

    public static <T> void replace(Collection<T> toReplace, Collection<T> replaceWith) {
        if (toReplace instanceof ImmutableCollection) {
            LogUtil.error("Tried to replace values inside of an ImmutableCollection");
            return;
        }
        toReplace.clear();
        toReplace.addAll(replaceWith);
    }

    public static <K, V> void replace(Map<K, V> toReplace, Map<K, V> replaceWith) {
        if (toReplace instanceof ImmutableMap) {
            LogUtil.error("Tried to replace values inside of an ImmutableMap");
            return;
        }
        toReplace.clear();
        toReplace.putAll(replaceWith);
    }
}
