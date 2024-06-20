/**
 *
 */
package util;

import java.util.*;

/**
 * @author subhro
 *
 */
public final class CollectionHelper {
    @SafeVarargs
    public static <E> List<E> arrayList(E... args) {
        List<E> list = new ArrayList<>();
        list.addAll(Arrays.asList(args));
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Collection<T> collection) {
        return collection.toArray((T[]) new Object[0]);
    }

    public static final class Dict<K, V> extends HashMap<K, V> {
        public Dict<K, V> putItem(K k, V v) {
            super.put(k, v);
            return this;
        }

        public V getItem(K k) {
            return super.get(k);
        }

        public V getItem(K k, V v) {
            V v1 = super.get(k);
            return v1 == null ? v : v1;
        }
    }

    public static final class OrderedDict<K, V> extends LinkedHashMap<K, V> {
        public OrderedDict<K, V> putItem(K k, V v) {
            super.put(k, v);
            return this;
        }

        public V getItem(K k) {
            return super.get(k);
        }

        public V getItem(K k, V v) {
            V v1 = super.get(k);
            return v1 == null ? v : v1;
        }
    }
}
