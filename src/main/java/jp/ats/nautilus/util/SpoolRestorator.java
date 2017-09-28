package jp.ats.nautilus.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import jp.ats.nautilus.common.CP932;
import jp.ats.nautilus.dds.HostHandler;
import jp.ats.nautilus.dds.HostResource;
import jp.ats.nautilus.dds.HostUtilities;

public class SpoolRestorator {

	private static final String filler = new String();

	private final List<String> lines = new LinkedList<>();

	private int currentPosition = 0;

	public void restore(
		HostHandler handler,
		Consumer<RestoredPage> pageConsumer)
		throws InterruptedException {
		HostResource resource = handler.getResource();
		resource.read(bytes -> {
			String line = CP932
				.treatForCP932(HostUtilities.convertLine(bytes, true));

			accept(line, pageConsumer);
		});

		finish(pageConsumer);
	}

	public void restore(
		BufferedReader reader,
		Consumer<RestoredPage> pageConsumer)
		throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			accept(line, pageConsumer);
		}

		finish(pageConsumer);
	}

	public void restore(
		Iterable<String> spool,
		Consumer<RestoredPage> pageConsumer) {
		spool.forEach(line -> accept(line, pageConsumer));
		finish(pageConsumer);
	}

	private void accept(String spoolLine, Consumer<RestoredPage> pageConsumer) {
		String header = spoolLine.substring(0, 5);
		String body = spoolLine.substring(5);

		int number = Integer.parseInt(header.trim());

		if (header.charAt(3) != ' ') {
			currentPosition += number;
		} else {
			boolean isNewPage = number < currentPosition + 1;

			currentPosition = number - 1;

			if (isNewPage) {
				//改ページ
				RestoredPage page = extractPage();
				setLine(body);
				pageConsumer.accept(page);
			}
		}

		setLine(body);
	}

	private void finish(Consumer<RestoredPage> pageConsumer) {
		pageConsumer.accept(extractPage());
	}

	private RestoredPage extractPage() {
		RestoredPage result = new RestoredPage(new ArrayList<>(lines));
		lines.clear();
		return result;
	}

	private void setLine(String line) {
		for (int i = lines.size(); i <= currentPosition; i++) {
			lines.add(filler);
		}

		String oldBody = lines.get(currentPosition);
		if (!oldBody.equals(filler)) line = mergeLine(oldBody, line);

		lines.set(currentPosition, line);
	}

	private static final Charset mergeCharset = Charset.forName("MS932");

	private static final byte whiteSpace = ' ';

	private static String mergeLine(String oldLine, String newLine) {
		byte[] oldBytes = oldLine.getBytes(mergeCharset);
		byte[] newBytes = newLine.getBytes(mergeCharset);
		byte[] merged = new byte[oldBytes.length > newBytes.length
			? oldBytes.length
			: newBytes.length];

		merge(oldBytes, merged);
		merge(newBytes, merged);

		return new String(merged, mergeCharset);
	}

	private static void merge(byte[] source, byte[] merged) {
		for (int i = 0; i < source.length; i++) {
			byte b = source[i];
			if (b != whiteSpace) {
				merged[i] = b;
			} else if (merged[i] == 0) {
				merged[i] = whiteSpace;
			}
		}
	}
}
