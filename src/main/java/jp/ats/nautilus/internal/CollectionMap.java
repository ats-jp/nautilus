package jp.ats.nautilus.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import jp.ats.nautilus.common.U;

import java.util.Set;

/**
 * @author 千葉 哲嗣
 */
public class CollectionMap<K, V> implements Cloneable {

	private final Map<K, Collection<V>> map;

	public static <V, K> CollectionMap<K, V> newInstance() {
		return new CollectionMap<K, V>();
	}

	public static <V, K> CollectionMap<K, V> newInstance(
		@SuppressWarnings("rawtypes") Class<? extends Map> mapClass) {
		return new CollectionMap<K, V>(mapClass);
	}

	public CollectionMap() {
		map = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	public CollectionMap(
		@SuppressWarnings("rawtypes") Class<? extends Map> mapClass) {
		try {
			this.map = mapClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void put(K key, V value) {
		Collection<V> collection = get(key);
		collection.add(value);
	}

	public Collection<V> get(K key) {
		Collection<V> collection = map.get(key);
		if (collection == null) {
			collection = createNewCollection();
			map.put(key, collection);
		}
		return collection;
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public Collection<V> remove(K key) {
		Collection<V> collection = map.remove(key);
		if (collection == null) return createNewCollection();
		return collection;
	}

	public int size() {
		return map.size();
	}

	public void clear() {
		map.clear();
	}

	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	public Map<K, Collection<V>> getInnerMap() {
		return Collections.unmodifiableMap(map);
	}

	@Override
	public CollectionMap<K, V> clone() {
		CollectionMap<K, V> clone = newInstance(map.getClass());
		for (Entry<K, Collection<V>> entry : map.entrySet()) {
			K key = entry.getKey();
			for (V value : entry.getValue()) {
				clone.put(key, value);
			}
		}
		return clone;
	}

	@Override
	public String toString() {
		return U.toString(this);
	}

	protected Collection<V> createNewCollection() {
		return new LinkedList<>();
	}
}
