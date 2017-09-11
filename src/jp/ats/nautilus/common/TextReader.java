package jp.ats.nautilus.common;

import static jp.ats.nautilus.common.U.newLinkedList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

/**
 * @author 千葉 哲嗣
 */
public final class TextReader {

	private final InputStream input;

	/**
	 * 指定されたクラスと同パッケージで同名のファイルをロードします。
	 *
	 * @param resourceBase テキストファイルと同パッケージで同名のクラス
	 * @param extension ロードするテキストファイルの拡張子
	 */
	public TextReader(Class<?> resourceBase, String extension)
		throws IOException {
		input = U.getResourcePath(resourceBase, extension).openStream();
	}

	/**
	 * @param url テキストファイルのURL
	 * @throws IllegalArgumentException resourcePathで指定されたファイルが存在しない場合
	 * @throws IllegalStateException 予測できない入出力例外が発生した場合
	 */
	public TextReader(URL url) throws IOException {
		input = url.openStream();
	}

	/**
	 * クラスパス内にあるテキストファイルをロードします。<br>
	 * テキストファイルのパスは、 "/" からはじまっている必要があります。
	 *
	 * @param path テキストファイルのパス
	 * @throws IllegalArgumentException resourcePathで指定されたファイルが存在しない場合
	 * @throws IllegalStateException 予測できない入出力例外が発生した場合
	 */
	public TextReader(String path) throws IOException {
		URL url = TextReader.class.getResource(path);
		if (url == null) {
			throw new IllegalArgumentException(path + " が見つかりませんでした");
		}

		input = url.openStream();
	}

	/**
	 * @param input 入力
	 */
	public TextReader(InputStream input) {
		this.input = input;
	}

	public String[] readLinesAsArray() throws IOException {
		return readLinesAsArray(Charset.defaultCharset());
	}

	public String[] readLinesAsArray(Charset charset) throws IOException {
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(input, charset));

		List<String> list = newLinkedList();
		for (String line; (line = reader.readLine()) != null;) {
			list.add(line);
		}

		U.close(reader);

		return list.toArray(new String[list.size()]);
	}

	public IterableIterator<String> readLinesAsIterator() {
		return readLinesAsIterator(Charset.defaultCharset());
	}

	public IterableIterator<String> readLinesAsIterator(Charset charset) {
		final BufferedReader reader = new BufferedReader(
			new InputStreamReader(input, charset));

		final String[] line = new String[1];

		return new IterableIterator<String>() {

			@Override
			public Iterator<String> iterator() {
				return this;
			}

			@Override
			public boolean hasNext() {
				boolean hasNext;
				try {
					hasNext = (line[0] = reader.readLine()) != null;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				if (!hasNext) U.close(reader);

				return hasNext;
			}

			@Override
			public String next() {
				return line[0];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public void readLines(Callback callback) throws IOException {
		readLines(callback, Charset.defaultCharset());
	}

	public void readLines(Callback callback, Charset charset)
		throws IOException {
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(input, charset));
		try {
			for (String line; (line = reader.readLine()) != null;) {
				callback.process(line);
			}
		} finally {
			U.close(reader);
		}
	}

	public void close() {
		U.close(input);
	}

	@Override
	protected void finalize() {
		close();
	}

	public static interface Callback {

		void process(String line);
	}
}
