package jp.ats.nautilus.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 千葉 哲嗣
 */
public class U {

	public static final String FILE_SEPARATOR = System
		.getProperty("file.separator");

	public static final String PATH_SEPARATOR = System
		.getProperty("path.separator");

	public static final String LINE_SEPARATOR = System
		.getProperty("line.separator");

	public static final boolean[] BOOLEAN_EMPTY_ARRAY = {};

	public static final byte[] BYTE_EMPTY_ARRAY = {};

	public static final char[] CHAR_EMPTY_ARRAY = {};

	public static final double[] DOUBLE_EMPTY_ARRAY = {};

	public static final float[] FLOAT_EMPTY_ARRAY = {};

	public static final int[] INT_EMPTY_ARRAY = {};

	public static final long[] LONG_EMPTY_ARRAY = {};

	public static final short[] SHORT_EMPTY_ARRAY = {};

	public static final Object[] OBJECT_EMPTY_ARRAY = {};

	public static final String[] STRING_EMPTY_ARRAY = {};

	public static final Class<?>[] CLASS_EMPTY_ARRAY = {};

	public static final int BUFFER_SIZE = 8192;

	private static final int hashCodeBase = 17;

	private static final int hashCodeSugar = 37;

	private static final Map<String, Class<?>> primitiveTypeMap = new HashMap<>();

	private static final Map<Class<?>, Class<?>> primitiveToWrapperMap = new HashMap<>();

	private static final ThreadLocal<Set<Container>> cycleCheckerThreadLocal = new ThreadLocal<Set<Container>>();

	private static final Pattern fileURLPattern = Pattern
		.compile("^file:(.*)$");

	private static final Map<Class<? extends Number>, Integer> numberMap = new HashMap<>();

	private static final ThreadLocal<Map<String, SimpleDateFormat>> dateFormats = new ThreadLocal<Map<String, SimpleDateFormat>>() {

		@Override
		protected Map<String, SimpleDateFormat> initialValue() {
			return new HashMap<>();
		}
	};

	static {
		primitiveTypeMap.put(boolean.class.getName(), boolean.class);
		primitiveTypeMap.put(byte.class.getName(), byte.class);
		primitiveTypeMap.put(char.class.getName(), char.class);
		primitiveTypeMap.put(short.class.getName(), short.class);
		primitiveTypeMap.put(int.class.getName(), int.class);
		primitiveTypeMap.put(long.class.getName(), long.class);
		primitiveTypeMap.put(float.class.getName(), float.class);
		primitiveTypeMap.put(double.class.getName(), double.class);
		primitiveTypeMap.put(void.class.getName(), void.class);

		primitiveTypeMap
			.put(boolean[].class.getCanonicalName(), boolean[].class);
		primitiveTypeMap.put(byte[].class.getCanonicalName(), byte[].class);
		primitiveTypeMap.put(char[].class.getCanonicalName(), char[].class);
		primitiveTypeMap.put(short[].class.getCanonicalName(), short[].class);
		primitiveTypeMap.put(int[].class.getCanonicalName(), int[].class);
		primitiveTypeMap.put(long[].class.getCanonicalName(), long[].class);
		primitiveTypeMap.put(float[].class.getCanonicalName(), float[].class);
		primitiveTypeMap.put(double[].class.getCanonicalName(), double[].class);
	}

	static {
		primitiveToWrapperMap.put(boolean.class, Boolean.class);
		primitiveToWrapperMap.put(byte.class, Byte.class);
		primitiveToWrapperMap.put(char.class, Character.class);
		primitiveToWrapperMap.put(short.class, Short.class);
		primitiveToWrapperMap.put(int.class, Integer.class);
		primitiveToWrapperMap.put(long.class, Long.class);
		primitiveToWrapperMap.put(float.class, Float.class);
		primitiveToWrapperMap.put(double.class, Double.class);
		primitiveToWrapperMap.put(void.class, Void.class);
	}

	static {
		numberMap.put(BigDecimal.class, 0);
		numberMap.put(BigInteger.class, 1);
		numberMap.put(Byte.class, 2);
		numberMap.put(Double.class, 3);
		numberMap.put(Float.class, 4);
		numberMap.put(Integer.class, 5);
		numberMap.put(Long.class, 6);
		numberMap.put(Short.class, 7);
	}

	private U() {}

	public static int sumHashCodes(int[] hashCodes) {
		int result = hashCodeBase;
		for (int hashCode : hashCodes)
			result = hashCodeSugar * result + hashCode;
		return result;
	}

	public static int sumHashCodes(Object[] members) {
		int result = hashCodeBase;
		for (Object member : members)
			result = hashCodeSugar * result
				+ (member == null ? 0 : member.hashCode());
		return result;
	}

	public static int sumHashCodes(Collection<?> members) {
		return sumHashCodes(members.toArray(new Object[members.size()]));
	}

	public static int sumHashCodesOf(int... hashCodes) {
		int result = hashCodeBase;
		for (int hashCode : hashCodes)
			result = hashCodeSugar * result + hashCode;
		return result;
	}

	public static boolean equals(Object[] objects, Object[] others) {
		if (objects == null && others == null) return true;
		if (objects == null || others == null) return false;
		if (objects.length != others.length) return false;
		objects = objects.clone();
		others = others.clone();
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] == null && others[i] == null) continue;
			if (objects[i] == null || !objects[i].equals(others[i]))
				return false;
		}

		return true;
	}

	public static Class<?> getPrimitiveClass(String type) {
		return primitiveTypeMap.get(type);
	}

	public static Class<?> convertPrimitiveClassToWrapperClass(
		Class<?> target) {
		if (!target.isPrimitive()) return target;
		return primitiveToWrapperMap.get(target);
	}

	/**
	 * デバッグ用の、簡易文字列化メソッドです。
	 * <br>
	 * リフレクションを利用して、内部のフィールド値を出力します。
	 * <br>
	 * 循環参照が発生している場合、二度目の出力時に {repetition} と出力されます。
	 * <br>
	 * 使用上の注意点：
	 * <br>
	 * このメソッドを使用するのはあくまでも用途をデバッグに限定してください。
	 * <br>
	 * また、{@link Object} 以外の親クラスを持つクラスでは、親クラスの toString() メソッドをオーバーライドする可能性があるので、このメソッドを呼ぶ toString() を定義しないほうが無難です。
	 *
	 * @param object 文字列化対象
	 * @return object の文字列表現
	 */
	public static String toString(Object object) {
		Map<String, Object> map = new TreeMap<>();

		boolean top = false;
		Set<Container> checker = cycleCheckerThreadLocal.get();
		if (checker == null) {
			checker = new HashSet<>();
			checker.add(new Container(object));
			cycleCheckerThreadLocal.set(checker);
			top = true;
		}

		try {
			getFields(object.getClass(), object, map, checker);
			return "{id:"
				+ System.identityHashCode(object)
				+ " "
				+ map.toString()
				+ "}";
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} finally {
			if (top) cycleCheckerThreadLocal.set(null);
		}
	}

	public static String care(String target) {
		return target == null ? "" : target;
	}

	public static String trim(String target) {
		return care(target).trim();
	}

	public static String[] care(String[] target) {
		String[] clone = target.clone();
		for (int i = 0; i < clone.length; i++) {
			clone[i] = care(clone[i]);
		}

		return clone;
	}

	public static boolean isAvailable(String value) {
		return value != null && !value.equals("");
	}

	public static boolean isAvailable(String... values) {
		for (String value : values) {
			if (isAvailable(value)) return true;
		}
		return false;
	}

	public static boolean isAllValuesAvailable(String... values) {
		for (String value : values) {
			if (!isAvailable(value)) return false;
		}
		return true;
	}

	/**
	 * 指定されたオブジェクトと同パッケージで同クラス名のファイルのパスを返します。
	 * @param resourceBase リソースファイルと同パッケージで同名のクラスのオブジェクト
	 * @param extension ロードするファイルの拡張子
	 */
	public static URL getResourcePath(Object resourceBase, String extension) {
		return getResourcePath(resourceBase.getClass(), extension);
	}

	/**
	 * 指定されたクラスと同パッケージで同名のファイルのパスを返します。
	 * @param resourceBase リソースファイルと同パッケージで同名のクラス
	 * @param extension ロードするファイルの拡張子
	 */
	public static URL getResourcePath(Class<?> resourceBase, String extension) {
		String path = "/"
			+ resourceBase.getName().replace('.', '/')
			+ "."
			+ extension;
		return U.class.getResource(path);
	}

	/**
	 * 指定されたオブジェクトと同パッケージで指定された名前のファイルのパスを返します。
	 * @param resourceBase リソースファイルと同パッケージ
	 * @param fileName ロードするファイル名
	 */
	public static URL getResourcePathByName(
		Object resourceBase,
		String fileName) {
		return getResourcePathByName(resourceBase.getClass(), fileName);
	}

	/**
	 * 指定されたクラスと同パッケージで指定された名前のファイルのパスを返します。
	 * @param resourceBase リソースファイルと同パッケージ
	 * @param fileName ロードするファイル名
	 */
	public static URL getResourcePathByName(
		Class<?> resourceBase,
		String fileName) {
		String path = "/"
			+ resourceBase.getPackage().getName().replace('.', '/')
			+ "/"
			+ fileName;
		return U.class.getResource(path);
	}

	/**
	 * URLがローカルファイルである場合、Fileオブジェクトとして返します。
	 * @param localFileURL ローカルファイルのURL
	 * @param encode URL の文字列表現に使用されるエンコード
	 * @return URLが指し示すFile
	 * @throws IllegalArgumentException パラメータがローカルファイルではない場合
	 */
	public static File getLocalFile(URL localFileURL, String encode) {
		Matcher matcher = fileURLPattern.matcher(localFileURL.toString());
		if (matcher.find()) {
			try {
				return new File(URLDecoder.decode(matcher.group(1), encode));
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException(e);
			}
		}
		throw new IllegalArgumentException(localFileURL.toString());
	}

	public static File getJARFile(Class<?> classInJAR) {
		String relativeLocation = "/"
			+ classInJAR.getName().replace('.', '/')
			+ ".class";

		String selfPath;
		try {
			selfPath = URLDecoder.decode(
				classInJAR.getResource(relativeLocation).toString(),
				"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}

		String schema = "jar:file:/";

		if (!selfPath.startsWith(schema))
			throw new IllegalStateException("起動方法に問題があります。");

		//jarファイルが置かれている場所を特定
		return new File(
			selfPath.substring(schema.length())
				.replaceAll("!" + relativeLocation + "$", ""));
	}

	public static <T> Enumeration<T> getMapKeys(Map<T, ?> map) {
		return toEnumeration(map.keySet().iterator());
	}

	public static <T> Enumeration<T> getElements(List<T> list) {
		return toEnumeration(list.iterator());
	}

	public static <T> Iterable<T> iterable(final Iterator<T> iterator) {
		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return iterator;
			}
		};
	}

	public static String join(String separator, Object... values) {
		return join(values, separator);
	}

	public static String join(Object[] values, String separator) {
		if (values.length == 0) return "";
		StringBuffer buffer = new StringBuffer();
		for (Object value : values) {
			buffer.append(separator);
			buffer.append(value);
		}

		String joined = buffer.toString();
		if (joined.length() == 0) return joined;

		return joined.substring(separator.length());
	}

	public static String join(Collection<?> values, String separator) {
		if (values.size() == 0) return "";
		StringBuffer buffer = new StringBuffer();
		for (Object value : values) {
			buffer.append(separator);
			buffer.append(value);
		}

		String joined = buffer.toString();
		if (joined.length() == 0) return joined;

		return joined.substring(separator.length());
	}

	public static <T> Enumeration<T> toEnumeration(final Iterator<T> i) {
		return new Enumeration<T>() {

			@Override
			public boolean hasMoreElements() {
				return i.hasNext();
			}

			@Override
			public T nextElement() {
				return i.next();
			}
		};
	}

	@SafeVarargs
	public static <T> T[] newArray(T... items) {
		return items;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] castToArray(Object target) {
		return (T[]) target;
	}

	@SafeVarargs
	public static <E> ArrayList<E> newArrayListOf(E... items) {
		return new ArrayList<E>(Arrays.asList(items));
	}

	@SuppressWarnings("unchecked")
	public static <E> ArrayList<E> cast(
		@SuppressWarnings("rawtypes") ArrayList target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <E> ArrayList<E> castToArrayList(Object target) {
		return (ArrayList<E>) target;
	}

	public static <E> LinkedList<E> newLinkedList() {
		return new LinkedList<E>();
	}

	@SafeVarargs
	public static <E> LinkedList<E> newLinkedListOf(E... items) {
		return new LinkedList<E>(Arrays.asList(items));
	}

	@SuppressWarnings("unchecked")
	public static <E> LinkedList<E> cast(
		@SuppressWarnings("rawtypes") LinkedList target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <E> LinkedList<E> castToLinkedList(Object target) {
		return (LinkedList<E>) target;
	}

	@SafeVarargs
	public static <E> HashSet<E> newHashSetOf(E... items) {
		return new HashSet<E>(Arrays.asList(items));
	}

	@SuppressWarnings("unchecked")
	public static <E> HashSet<E> cast(
		@SuppressWarnings("rawtypes") HashSet target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <E> HashSet<E> castToHashSet(Object target) {
		return (HashSet<E>) target;
	}

	@SafeVarargs
	public static <E> LinkedHashSet<E> newLinkedHashSetOf(E... items) {
		return new LinkedHashSet<E>(Arrays.asList(items));
	}

	@SuppressWarnings("unchecked")
	public static <E> LinkedHashSet<E> cast(
		@SuppressWarnings("rawtypes") LinkedHashSet target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <E> LinkedHashSet<E> castToLinkedHashSet(Object target) {
		return (LinkedHashSet<E>) target;
	}

	@SafeVarargs
	public static <E> TreeSet<E> newTreeSetOf(E... items) {
		return new TreeSet<E>(Arrays.asList(items));
	}

	@SuppressWarnings("unchecked")
	public static <E> TreeSet<E> cast(
		@SuppressWarnings("rawtypes") TreeSet target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <E> TreeSet<E> castToTreeSet(Object target) {
		return (TreeSet<E>) target;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> HashMap<K, V> cast(
		@SuppressWarnings("rawtypes") HashMap target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> HashMap<K, V> castToHashMap(Object target) {
		return (HashMap<K, V>) target;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> LinkedHashMap<K, V> cast(
		@SuppressWarnings("rawtypes") LinkedHashMap target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> LinkedHashMap<K, V> castToLinkedHashMap(
		Object target) {
		return (LinkedHashMap<K, V>) target;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> TreeMap<K, V> cast(
		@SuppressWarnings("rawtypes") TreeMap target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> TreeMap<K, V> castToTreeMap(Object target) {
		return (TreeMap<K, V>) target;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> WeakHashMap<K, V> cast(
		@SuppressWarnings("rawtypes") WeakHashMap target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> WeakHashMap<K, V> castToWeakHashMap(Object target) {
		return (WeakHashMap<K, V>) target;
	}

	@SuppressWarnings("unchecked")
	public static <E> Enumeration<E> cast(
		@SuppressWarnings("rawtypes") Enumeration target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <E> Enumeration<E> castToEnumeration(Object target) {
		return (Enumeration<E>) target;
	}

	@SuppressWarnings("unchecked")
	public static <E> Iterator<E> cast(
		@SuppressWarnings("rawtypes") Iterator target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <E> Iterator<E> castToIterator(Object target) {
		return (Iterator<E>) target;
	}

	@SuppressWarnings("unchecked")
	public static <E> Iterable<E> cast(
		@SuppressWarnings("rawtypes") Iterable target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <E> Iterable<E> castToIterable(Object target) {
		return (Iterable<E>) target;
	}

	@SuppressWarnings("unchecked")
	public static <E> Collection<E> cast(
		@SuppressWarnings("rawtypes") Collection target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <E> Collection<E> castToCollection(Object target) {
		return (Collection<E>) target;
	}

	@SuppressWarnings("unchecked")
	public static <E> List<E> cast(@SuppressWarnings("rawtypes") List target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <E> List<E> castToList(Object target) {
		return (List<E>) target;
	}

	@SuppressWarnings("unchecked")
	public static <E> Set<E> cast(@SuppressWarnings("rawtypes") Set target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <E> Set<E> castToSet(Object target) {
		return (Set<E>) target;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> cast(
		@SuppressWarnings("rawtypes") Map target) {
		return target;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> castToMap(Object target) {
		return (Map<K, V>) target;
	}

	/**
	 * 先頭に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	@SafeVarargs
	public static <T> T[] unshift(T[] src, T... newElements) {
		T[] dest = newArray(src, src.length + newElements.length);
		System.arraycopy(src, 0, dest, newElements.length, src.length);
		System.arraycopy(newElements, 0, dest, 0, newElements.length);
		return dest;
	}

	/**
	 * 末尾に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	@SafeVarargs
	public static <T> T[] push(T[] src, T... newElements) {
		T[] dest = newArray(src, src.length + newElements.length);
		System.arraycopy(src, 0, dest, 0, src.length);
		System.arraycopy(newElements, 0, dest, src.length, newElements.length);
		return dest;
	}

	/**
	 * 先頭から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static <T> T[] shift(T[] src) {
		int newLength = src.length - 1;
		T[] dest = newArray(src, newLength);
		System.arraycopy(src, 1, dest, 0, newLength);
		return dest;
	}

	/**
	 * 末尾から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static <T> T[] pop(T[] src) {
		int newLength = src.length - 1;
		T[] dest = newArray(src, newLength);
		System.arraycopy(src, 0, dest, 0, newLength);
		return dest;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] newArray(Class<T> componentType, int length) {
		return (T[]) Array.newInstance(componentType, length);
	}

	/**
	 * 先頭に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static boolean[] unshift(boolean[] src, boolean... newElements) {
		boolean[] dest = new boolean[src.length + newElements.length];
		System.arraycopy(src, 0, dest, newElements.length, src.length);
		System.arraycopy(newElements, 0, dest, 0, newElements.length);
		return dest;
	}

	/**
	 * 末尾に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static boolean[] push(boolean[] src, boolean... newElements) {
		boolean[] dest = new boolean[src.length + newElements.length];
		System.arraycopy(src, 0, dest, 0, src.length);
		System.arraycopy(newElements, 0, dest, src.length, newElements.length);
		return dest;
	}

	/**
	 * 先頭から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static boolean[] shift(boolean[] src) {
		int newLength = src.length - 1;
		boolean[] dest = new boolean[newLength];
		System.arraycopy(src, 1, dest, 0, newLength);
		return dest;
	}

	/**
	 * 末尾から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static boolean[] pop(boolean[] src) {
		int newLength = src.length - 1;
		boolean[] dest = new boolean[newLength];
		System.arraycopy(src, 0, dest, 0, newLength);
		return dest;
	}

	/**
	 * プリミティブ型配列をラッパー型配列に変換します。
	 *
	 * @param src プリミティブ型配列
	 * @return ラッパー型配列
	 */
	public static Boolean[] box(boolean[] src) {
		Boolean[] boxed = new Boolean[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * ラッパー型配列をプリミティブ型配列に変換します。
	 *
	 * @param src ラッパー型配列
	 * @return プリミティブ型配列
	 */
	public static boolean[] unbox(Boolean[] src) {
		boolean[] boxed = new boolean[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * 先頭に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static byte[] unshift(byte[] src, byte... newElements) {
		byte[] dest = new byte[src.length + newElements.length];
		System.arraycopy(src, 0, dest, newElements.length, src.length);
		System.arraycopy(newElements, 0, dest, 0, newElements.length);
		return dest;
	}

	/**
	 * 末尾に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static byte[] push(byte[] src, byte... newElements) {
		byte[] dest = new byte[src.length + newElements.length];
		System.arraycopy(src, 0, dest, 0, src.length);
		System.arraycopy(newElements, 0, dest, src.length, newElements.length);
		return dest;
	}

	/**
	 * 先頭から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static byte[] shift(byte[] src) {
		int newLength = src.length - 1;
		byte[] dest = new byte[newLength];
		System.arraycopy(src, 1, dest, 0, newLength);
		return dest;
	}

	/**
	 * 末尾から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static byte[] pop(byte[] src) {
		int newLength = src.length - 1;
		byte[] dest = new byte[newLength];
		System.arraycopy(src, 0, dest, 0, newLength);
		return dest;
	}

	/**
	 * プリミティブ型配列をラッパー型配列に変換します。
	 *
	 * @param src プリミティブ型配列
	 * @return ラッパー型配列
	 */
	public static Byte[] box(byte[] src) {
		Byte[] boxed = new Byte[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * ラッパー型配列をプリミティブ型配列に変換します。
	 *
	 * @param src ラッパー型配列
	 * @return プリミティブ型配列
	 */
	public static byte[] unbox(Byte[] src) {
		byte[] boxed = new byte[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * 先頭に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static char[] unshift(char[] src, char... newElements) {
		char[] dest = new char[src.length + newElements.length];
		System.arraycopy(src, 0, dest, newElements.length, src.length);
		System.arraycopy(newElements, 0, dest, 0, newElements.length);
		return dest;
	}

	/**
	 * 末尾に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static char[] push(char[] src, char... newElements) {
		char[] dest = new char[src.length + newElements.length];
		System.arraycopy(src, 0, dest, 0, src.length);
		System.arraycopy(newElements, 0, dest, src.length, newElements.length);
		return dest;
	}

	/**
	 * 先頭から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static char[] shift(char[] src) {
		int newLength = src.length - 1;
		char[] dest = new char[newLength];
		System.arraycopy(src, 1, dest, 0, newLength);
		return dest;
	}

	/**
	 * 末尾から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static char[] pop(char[] src) {
		int newLength = src.length - 1;
		char[] dest = new char[newLength];
		System.arraycopy(src, 0, dest, 0, newLength);
		return dest;
	}

	/**
	 * プリミティブ型配列をラッパー型配列に変換します。
	 *
	 * @param src プリミティブ型配列
	 * @return ラッパー型配列
	 */
	public static Character[] box(char[] src) {
		Character[] boxed = new Character[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * ラッパー型配列をプリミティブ型配列に変換します。
	 *
	 * @param src ラッパー型配列
	 * @return プリミティブ型配列
	 */
	public static char[] unbox(Character[] src) {
		char[] boxed = new char[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * 先頭に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static double[] unshift(double[] src, double... newElements) {
		double[] dest = new double[src.length + newElements.length];
		System.arraycopy(src, 0, dest, newElements.length, src.length);
		System.arraycopy(newElements, 0, dest, 0, newElements.length);
		return dest;
	}

	/**
	 * 末尾に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static double[] push(double[] src, double... newElements) {
		double[] dest = new double[src.length + newElements.length];
		System.arraycopy(src, 0, dest, 0, src.length);
		System.arraycopy(newElements, 0, dest, src.length, newElements.length);
		return dest;
	}

	/**
	 * 先頭から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static double[] shift(double[] src) {
		int newLength = src.length - 1;
		double[] dest = new double[newLength];
		System.arraycopy(src, 1, dest, 0, newLength);
		return dest;
	}

	/**
	 * 末尾から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static double[] pop(double[] src) {
		int newLength = src.length - 1;
		double[] dest = new double[newLength];
		System.arraycopy(src, 0, dest, 0, newLength);
		return dest;
	}

	/**
	 * プリミティブ型配列をラッパー型配列に変換します。
	 *
	 * @param src プリミティブ型配列
	 * @return ラッパー型配列
	 */
	public static Double[] box(double[] src) {
		Double[] boxed = new Double[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * ラッパー型配列をプリミティブ型配列に変換します。
	 *
	 * @param src ラッパー型配列
	 * @return プリミティブ型配列
	 */
	public static double[] unbox(Double[] src) {
		double[] boxed = new double[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * 先頭に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static float[] unshift(float[] src, float... newElements) {
		float[] dest = new float[src.length + newElements.length];
		System.arraycopy(src, 0, dest, newElements.length, src.length);
		System.arraycopy(newElements, 0, dest, 0, newElements.length);
		return dest;
	}

	/**
	 * 末尾に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static float[] push(float[] src, float... newElements) {
		float[] dest = new float[src.length + newElements.length];
		System.arraycopy(src, 0, dest, 0, src.length);
		System.arraycopy(newElements, 0, dest, src.length, newElements.length);
		return dest;
	}

	/**
	 * 先頭から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static float[] shift(float[] src) {
		int newLength = src.length - 1;
		float[] dest = new float[newLength];
		System.arraycopy(src, 1, dest, 0, newLength);
		return dest;
	}

	/**
	 * 末尾から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static float[] pop(float[] src) {
		int newLength = src.length - 1;
		float[] dest = new float[newLength];
		System.arraycopy(src, 0, dest, 0, newLength);
		return dest;
	}

	/**
	 * プリミティブ型配列をラッパー型配列に変換します。
	 *
	 * @param src プリミティブ型配列
	 * @return ラッパー型配列
	 */
	public static Float[] box(float[] src) {
		Float[] boxed = new Float[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * ラッパー型配列をプリミティブ型配列に変換します。
	 *
	 * @param src ラッパー型配列
	 * @return プリミティブ型配列
	 */
	public static float[] unbox(Float[] src) {
		float[] boxed = new float[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * 先頭に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static int[] unshift(int[] src, int... newElements) {
		int[] dest = new int[src.length + newElements.length];
		System.arraycopy(src, 0, dest, newElements.length, src.length);
		System.arraycopy(newElements, 0, dest, 0, newElements.length);
		return dest;
	}

	/**
	 * 末尾に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static int[] push(int[] src, int... newElements) {
		int[] dest = new int[src.length + newElements.length];
		System.arraycopy(src, 0, dest, 0, src.length);
		System.arraycopy(newElements, 0, dest, src.length, newElements.length);
		return dest;
	}

	/**
	 * 先頭から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static int[] shift(int[] src) {
		int newLength = src.length - 1;
		int[] dest = new int[newLength];
		System.arraycopy(src, 1, dest, 0, newLength);
		return dest;
	}

	/**
	 * 末尾から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static int[] pop(int[] src) {
		int newLength = src.length - 1;
		int[] dest = new int[newLength];
		System.arraycopy(src, 0, dest, 0, newLength);
		return dest;
	}

	/**
	 * プリミティブ型配列をラッパー型配列に変換します。
	 *
	 * @param src プリミティブ型配列
	 * @return ラッパー型配列
	 */
	public static Integer[] box(int[] src) {
		Integer[] boxed = new Integer[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * ラッパー型配列をプリミティブ型配列に変換します。
	 *
	 * @param src ラッパー型配列
	 * @return プリミティブ型配列
	 */
	public static int[] unbox(Integer[] src) {
		int[] boxed = new int[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * 先頭に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static long[] unshift(long[] src, long... newElements) {
		long[] dest = new long[src.length + newElements.length];
		System.arraycopy(src, 0, dest, newElements.length, src.length);
		System.arraycopy(newElements, 0, dest, 0, newElements.length);
		return dest;
	}

	/**
	 * 末尾に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static long[] push(long[] src, long... newElements) {
		long[] dest = new long[src.length + newElements.length];
		System.arraycopy(src, 0, dest, 0, src.length);
		System.arraycopy(newElements, 0, dest, src.length, newElements.length);
		return dest;
	}

	/**
	 * 先頭から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static long[] shift(long[] src) {
		int newLength = src.length - 1;
		long[] dest = new long[newLength];
		System.arraycopy(src, 1, dest, 0, newLength);
		return dest;
	}

	/**
	 * 末尾から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static long[] pop(long[] src) {
		int newLength = src.length - 1;
		long[] dest = new long[newLength];
		System.arraycopy(src, 0, dest, 0, newLength);
		return dest;
	}

	/**
	 * プリミティブ型配列をラッパー型配列に変換します。
	 *
	 * @param src プリミティブ型配列
	 * @return ラッパー型配列
	 */
	public static Long[] box(long[] src) {
		Long[] boxed = new Long[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * ラッパー型配列をプリミティブ型配列に変換します。
	 *
	 * @param src ラッパー型配列
	 * @return プリミティブ型配列
	 */
	public static long[] unbox(Long[] src) {
		long[] boxed = new long[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * 先頭に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static short[] unshift(short[] src, short... newElements) {
		short[] dest = new short[src.length + newElements.length];
		System.arraycopy(src, 0, dest, newElements.length, src.length);
		System.arraycopy(newElements, 0, dest, 0, newElements.length);
		return dest;
	}

	/**
	 * 末尾に要素を追加した配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @param newElements 新要素
	 * @return 新配列
	 */
	public static short[] push(short[] src, short... newElements) {
		short[] dest = new short[src.length + newElements.length];
		System.arraycopy(src, 0, dest, 0, src.length);
		System.arraycopy(newElements, 0, dest, src.length, newElements.length);
		return dest;
	}

	/**
	 * 先頭から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static short[] shift(short[] src) {
		int newLength = src.length - 1;
		short[] dest = new short[newLength];
		System.arraycopy(src, 1, dest, 0, newLength);
		return dest;
	}

	/**
	 * 末尾から要素を取り除いた配列を生成して返します。
	 *
	 * @param src 元の配列
	 * @return 新配列
	 */
	public static short[] pop(short[] src) {
		int newLength = src.length - 1;
		short[] dest = new short[newLength];
		System.arraycopy(src, 0, dest, 0, newLength);
		return dest;
	}

	/**
	 * プリミティブ型配列をラッパー型配列に変換します。
	 *
	 * @param src プリミティブ型配列
	 * @return ラッパー型配列
	 */
	public static Short[] box(short[] src) {
		Short[] boxed = new Short[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	/**
	 * ラッパー型配列をプリミティブ型配列に変換します。
	 *
	 * @param src ラッパー型配列
	 * @return プリミティブ型配列
	 */
	public static short[] unbox(Short[] src) {
		short[] boxed = new short[src.length];
		for (int i = 0; i < src.length; i++) {
			boxed[i] = src[i];
		}

		return boxed;
	}

	public static void close(Closeable object) {
		if (object == null) return;
		try {
			object.close();
		} catch (IOException e) {
			throw new CloseFailedException(e);
		}
	}

	public static void flush(Flushable object) {
		if (object == null) return;
		try {
			object.flush();
		} catch (IOException e) {
			throw new CloseFailedException(e);
		}
	}

	/**
	 * このストリームから読み込めるだけ読み込み、byte 配列として返します。
	 */
	public static byte[] readBytes(InputStream in) throws IOException {
		byte[] concat = BYTE_EMPTY_ARRAY;
		byte[] b = new byte[BUFFER_SIZE];
		int readed;
		while ((readed = in.read(b, 0, BUFFER_SIZE)) > 0) {
			concat = concatByteArray(concat, concat.length, b, readed);
		}
		return concat;
	}

	/**
	 * in から buffer サイズ分を buffer に読み込みます。
	 * 読み込み途中でストリームの終わりに達した場合、そこまでに読み込まれた
	 * バイト数を返します。
	 * @param in 入力ストリーム
	 * @param buffer データ格納用バッファ
	 * @return バッファに読み込まれたバイトの合計数。ストリームの終わりに達してデータがない場合は -1
	 * @throws IOException
	 */
	public static int readBytes(InputStream in, byte[] buffer)
		throws IOException {
		return readBytes(in, buffer, buffer.length);
	}

	/**
	 * in から指定された size 分を buffer に読み込みます。
	 * 読み込み途中でストリームの終わりに達した場合、そこまでに読み込まれた
	 * バイト数を返します。
	 * @param in 入力ストリーム
	 * @param buffer データ格納用バッファ
	 * @param size 読み込み要求サイズ
	 * @return バッファに読み込まれたバイトの合計数。ストリームの終わりに達してデータがない場合は -1
	 * @throws IOException
	 */
	public static int readBytes(InputStream in, byte[] buffer, int size)
		throws IOException {
		int total = 0;
		while (total < size) {
			int readed = in.read(buffer, total, size - total);
			if (readed == -1) {
				if (total == 0) {
					return -1;
				}
				return total;
			}
			total += readed;
		}
		return total;
	}

	/**
	 * in から読み込めるだけ読み込み、out へ出力します。
	 */
	public static void sendBytes(InputStream in, OutputStream out)
		throws IOException {
		byte[] b = new byte[BUFFER_SIZE];
		int readed;
		while ((readed = in.read(b, 0, BUFFER_SIZE)) > 0) {
			out.write(b, 0, readed);
		}
		out.flush();
	}

	/**
	 * このストリームから読み込めるだけ読み込み、char 配列として返します。
	 */
	public static char[] readChars(Reader in) throws IOException {
		char[] concat = CHAR_EMPTY_ARRAY;
		char[] b = new char[BUFFER_SIZE];
		int readed;
		while ((readed = in.read(b, 0, BUFFER_SIZE)) > 0) {
			concat = concatCharArray(concat, concat.length, b, readed);
		}
		return concat;
	}

	/**
	 * in から buffer サイズ分を buffer に読み込みます。
	 * 読み込み途中でストリームの終わりに達した場合、そこまでに読み込まれた
	 * 文字数を返します。
	 * @param in 入力ストリーム
	 * @param buffer データ格納用バッファ
	 * @return バッファに読み込まれた文字の合計数。ストリームの終わりに達してデータがない場合は -1
	 * @throws IOException
	 */
	public static int readChars(Reader in, char[] buffer) throws IOException {
		return readChars(in, buffer, buffer.length);
	}

	/**
	 * in から指定された size 分を buffer に読み込みます。
	 * 読み込み途中でストリームの終わりに達した場合、そこまでに読み込まれた
	 * 文字数を返します。
	 * @param in 入力ストリーム
	 * @param buffer データ格納用バッファ
	 * @param size 読み込み要求サイズ
	 * @return バッファに読み込まれた文字の合計数。ストリームの終わりに達してデータがない場合は -1
	 * @throws IOException
	 */
	public static int readChars(Reader in, char[] buffer, int size)
		throws IOException {
		int total = 0;
		while (total < size) {
			int readed = in.read(buffer, total, size - total);
			if (readed == -1) {
				if (total == 0) {
					return -1;
				}
				return total;
			}
			total += readed;
		}
		return total;
	}

	/**
	 * in から読み込めるだけ読み込み、out へ出力します。
	 */
	public static void sendChars(Reader in, Writer out) throws IOException {
		char[] b = new char[BUFFER_SIZE];
		int readed;
		while ((readed = in.read(b, 0, BUFFER_SIZE)) > 0) {
			out.write(b, 0, readed);
		}
		out.flush();
	}

	/**
	 * クラスをロードするために使用される検索パスから、指定された名前のリソースをファイルとして取得します。
	 * このメソッドは実ファイルを対象とする場合のみ有効です。
	 *
	 * @param resource リソースの名前
	 * @return リソースに対応する {@link File} オブジェクト。リソースが見つからなかった場合は null
	 */
	public static File getResourceAsFile(String resource) {
		return getResourceAsFile(U.class.getResource(resource));
	}

	/**
	 * {@link URL} として指定されたリソースをファイルとして取得します。
	 * このメソッドは実ファイルを対象とする場合のみ有効です。
	 *
	 * @param resource リソースの {@link URL}
	 * @return リソースに対応する {@link File} オブジェクト。リソースが見つからなかった場合は null
	 */
	public static File getResourceAsFile(URL resource) {
		if (resource == null) return null;

		Matcher matcher = Pattern.compile("^file:(.*)$")
			.matcher(resource.toString());
		if (matcher.find()) {
			try {
				return new File(URLDecoder.decode(matcher.group(1), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new Error(e);
			}
		}
		return null;
	}

	public static Iterable<String> createLineIterable(final Reader in) {
		final Iterator<String> iterator = createLineIterator(in);
		return new Iterable<String>() {

			@Override
			public Iterator<String> iterator() {
				return iterator;
			}
		};
	}

	public static Iterator<String> createLineIterator(Reader in) {
		return new LineIterator(new BufferedReader(in));
	}

	public static void copy(File src, File dest) throws IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new BufferedInputStream(new FileInputStream(src));

			if (!dest.exists()) dest.createNewFile();
			output = new BufferedOutputStream(new FileOutputStream(dest));

			sendBytes(input, output);
		} finally {
			String message = "";
			boolean occurred = false;
			try {
				close(input);
			} catch (CloseFailedException e) {
				message += e.getMessage();
				occurred = true;
			}

			try {
				close(output);
			} catch (CloseFailedException e) {
				message += e.getMessage();
				occurred = true;
			}

			if (occurred) throw new CloseFailedException(message);
		}
	}

	public static void close(Connection connection) {
		if (connection == null) return;
		try {
			if (!connection.isClosed()) connection.close();
		} catch (SQLException e) {
			throw new JDBCCloseFailedException(e);
		}
	}

	public static void close(Statement statement) {
		if (statement == null) return;
		try {
			statement.close();
		} catch (SQLException e) {
			throw new JDBCCloseFailedException(e);
		}
	}

	public static void close(PreparedStatement statement) {
		if (statement == null) return;
		try {
			statement.close();
		} catch (SQLException e) {
			throw new JDBCCloseFailedException(e);
		}
	}

	public static void close(ResultSet result) {
		if (result == null) return;
		try {
			result.close();
		} catch (SQLException e) {
			throw new JDBCCloseFailedException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T cast(Object value) {
		return (T) value;
	}

	@SuppressWarnings("unchecked")
	public static <T> T convert(Class<T> castTo, Object value) {
		if (value == null) return null;
		Integer type = numberMap.get(castTo);
		if (type == null) return (T) value;
		switch (type) {
		case 0:
			return (T) convertToBigDecimal(value);
		case 1:
			return (T) convertToBigInteger(value);
		case 2:
			return (T) convertToByte(value);
		case 3:
			return (T) convertToDouble(value);
		case 4:
			return (T) convertToFloat(value);
		case 5:
			return (T) convertToInteger(value);
		case 6:
			return (T) convertToLong(value);
		case 7:
			return (T) convertToShort(value);
		default:
			throw new Error();
		}
	}

	public static BigDecimal convertToBigDecimal(Object value) {
		if (value == null) return null;
		if (value instanceof BigDecimal) return (BigDecimal) value;
		return new BigDecimal(value.toString());
	}

	public static BigInteger convertToBigInteger(Object value) {
		if (value == null) return null;
		if (value instanceof BigDecimal) return (BigInteger) value;
		return new BigInteger(value.toString());
	}

	public static Byte convertToByte(Object value) {
		if (value == null) return null;
		if (value instanceof Byte) return (Byte) value;
		if (value instanceof Number) return ((Number) value).byteValue();
		return Byte.valueOf(value.toString());
	}

	public static Double convertToDouble(Object value) {
		if (value == null) return null;
		if (value instanceof Double) return (Double) value;
		if (value instanceof Number) return ((Number) value).doubleValue();
		return Double.valueOf(value.toString());
	}

	public static Float convertToFloat(Object value) {
		if (value == null) return null;
		if (value instanceof Float) return (Float) value;
		if (value instanceof Number) return ((Number) value).floatValue();
		return Float.valueOf(value.toString());
	}

	public static Integer convertToInteger(Object value) {
		if (value == null) return null;
		if (value instanceof Integer) return (Integer) value;
		if (value instanceof Number) return ((Number) value).intValue();
		return Integer.valueOf(value.toString());
	}

	public static Long convertToLong(Object value) {
		if (value == null) return null;
		if (value instanceof Long) return (Long) value;
		if (value instanceof Number) return ((Number) value).longValue();
		return Long.valueOf(value.toString());
	}

	public static Short convertToShort(Object value) {
		if (value == null) return null;
		if (value instanceof Short) return (Short) value;
		if (value instanceof Number) return ((Number) value).shortValue();
		return Short.valueOf(value.toString());
	}

	public static String formatDate(String format, Date date) {
		return getDateFormat(format).format(date);
	}

	public static Date parseDate(String format, String date) {
		try {
			return getDateFormat(format).parse(date);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static Date parseDateStrictly(String format, String date)
		throws ParseException {
		return getDateFormat(format).parse(date);
	}

	public static byte[] concatByteArray(
		byte[] array1,
		int lengthof1,
		byte[] array2,
		int lengthof2) {
		byte[] concat = new byte[lengthof1 + lengthof2];
		System.arraycopy(array1, 0, concat, 0, lengthof1);
		System.arraycopy(array2, 0, concat, lengthof1, lengthof2);
		return concat;
	}

	public static char[] concatCharArray(
		char[] array1,
		int lengthof1,
		char[] array2,
		int lengthof2) {
		char[] concat = new char[lengthof1 + lengthof2];
		System.arraycopy(array1, 0, concat, 0, lengthof1);
		System.arraycopy(array2, 0, concat, lengthof1, lengthof2);
		return concat;
	}

	/**
	* intをバイト配列にします。
	*/
	public static byte[] toBytes(int integer) {
		byte[] bytes = new byte[4];
		bytes[3] = (byte) (0x000000ff & (integer));
		bytes[2] = (byte) (0x000000ff & (integer >>> 8));
		bytes[1] = (byte) (0x000000ff & (integer >>> 16));
		bytes[0] = (byte) (0x000000ff & (integer >>> 24));
		return bytes;
	}

	/**
	* バイトの配列をintにします。
	*/
	public static int toInt(byte[] bytes) {
		return ByteBuffer.wrap(bytes).asIntBuffer().get();
	}

	public static boolean[] booleanArray(Iterator<Boolean> iterator) {
		List<Boolean> list = U.newLinkedList();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}

		boolean[] array = new boolean[list.size()];

		for (int i = 0; i < array.length; i++) {
			array[i] = list.get(i);
		}

		return array;
	}

	public static byte[] byteArray(Iterator<Byte> iterator) {
		List<Byte> list = U.newLinkedList();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}

		byte[] array = new byte[list.size()];

		for (int i = 0; i < array.length; i++) {
			array[i] = list.get(i);
		}

		return array;
	}

	public static char[] charArray(Iterator<Character> iterator) {
		List<Character> list = U.newLinkedList();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}

		char[] array = new char[list.size()];

		for (int i = 0; i < array.length; i++) {
			array[i] = list.get(i);
		}

		return array;
	}

	public static double[] doubleArray(Iterator<Double> iterator) {
		List<Double> list = U.newLinkedList();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}

		double[] array = new double[list.size()];

		for (int i = 0; i < array.length; i++) {
			array[i] = list.get(i);
		}

		return array;
	}

	public static float[] floatArray(Iterator<Float> iterator) {
		List<Float> list = U.newLinkedList();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}

		float[] array = new float[list.size()];

		for (int i = 0; i < array.length; i++) {
			array[i] = list.get(i);
		}

		return array;
	}

	public static int[] intArray(Iterator<Integer> iterator) {
		List<Integer> list = U.newLinkedList();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}

		int[] array = new int[list.size()];

		for (int i = 0; i < array.length; i++) {
			array[i] = list.get(i);
		}

		return array;
	}

	public static long[] longArray(Iterator<Long> iterator) {
		List<Long> list = U.newLinkedList();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}

		long[] array = new long[list.size()];

		for (int i = 0; i < array.length; i++) {
			array[i] = list.get(i);
		}

		return array;
	}

	public static short[] shortArray(Iterator<Short> iterator) {
		List<Short> list = U.newLinkedList();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}

		short[] array = new short[list.size()];

		for (int i = 0; i < array.length; i++) {
			array[i] = list.get(i);
		}

		return array;
	}

	public static <T> T newInstance(String className) {
		try {
			Class<?> clazz = Class.forName(className);

			@SuppressWarnings("unchecked")
			T instance = (T) clazz.newInstance();

			return instance;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static class LineIterator implements Iterator<String> {

		private final BufferedReader reader;

		private String line;

		private LineIterator(BufferedReader reader) {
			this.reader = reader;
		}

		@Override
		public boolean hasNext() {
			try {
				line = reader.readLine();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			return line != null;
		}

		@Override
		public String next() {
			return line;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] newArray(T[] src, int length) {
		return newArray((Class<T>) src.getClass().getComponentType(), length);
	}

	private static void getFields(
		Class<?> clazz,
		Object object,
		Map<String, Object> map,
		Set<Container> checker)
		throws IllegalAccessException {
		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null) getFields(superclass, object, map, checker);
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers())) continue;
			field.setAccessible(true);
			Object value = field.get(object);
			//循環参照を避けるため、一度調査したオブジェクトは使用しない
			if (value != null) {
				Container container = new Container(value);
				if (checker.contains(container)) {
					map.put(field.getName(), "{repetition}");
					continue;
				}
				checker.add(container);
			}

			map.put(field.getName(), value);
		}
	}

	private static SimpleDateFormat getDateFormat(String format) {
		Map<String, SimpleDateFormat> map = dateFormats.get();
		SimpleDateFormat formatObject = map.get(format);
		if (formatObject == null) {
			formatObject = new SimpleDateFormat(format);
			map.put(format, formatObject);
		}

		return formatObject;
	}

	public static class CloseFailedException extends RuntimeException {

		private static final long serialVersionUID = -748991448426048317L;

		private CloseFailedException(IOException e) {
			super(e);
		}

		private CloseFailedException(String message) {
			super(message);
		}
	}

	public static class JDBCCloseFailedException extends RuntimeException {

		private static final long serialVersionUID = 1616600547104822010L;

		private JDBCCloseFailedException(SQLException e) {
			super(e);
		}
	}

	private static class Container {

		private final Object value;

		private Container(Object value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			return value == ((Container) o).value;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(value);
		}
	}
}
