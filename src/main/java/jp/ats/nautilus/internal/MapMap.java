package jp.ats.nautilus.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jp.ats.nautilus.common.U;

/**
 * @author 千葉 哲嗣
 */
public class MapMap<K1, K2, V> {

	private final Map<K1, Map<K2, V>> map;

	public static <K1, K2, V> MapMap<K1, K2, V> newInstance() {
		return new MapMap<K1, K2, V>();
	}

	public static <K1, K2, V> MapMap<K1, K2, V> newInstance(
		@SuppressWarnings("rawtypes") Class<? extends Map> mapClass) {
		return new MapMap<K1, K2, V>(mapClass);
	}

	public MapMap() {
		map = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	public MapMap(@SuppressWarnings("rawtypes") Class<? extends Map> mapClass) {
		try {
			this.map = mapClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Map<K2, V> get(K1 key) {
		Map<K2, V> sub = map.get(key);
		if (sub == null) {
			sub = createNewMap();
			map.put(key, sub);
		}
		return sub;
	}

	public Set<K1> keySet() {
		return map.keySet();
	}

	public Map<K2, V> remove(K1 key) {
		Map<K2, V> sub = map.remove(key);
		if (sub == null) return createNewMap();
		return sub;
	}

	public int size() {
		return map.size();
	}

	public void clear() {
		map.clear();
	}

	public boolean containsKey(K1 key) {
		return map.containsKey(key);
	}

	public Map<K1, Map<K2, V>> getInnerMap() {
		return Collections.unmodifiableMap(map);
	}

	@Override
	public String toString() {
		return U.toString(this);
	}

	protected Map<K2, V> createNewMap() {
		return new HashMap<>();
	}
}
