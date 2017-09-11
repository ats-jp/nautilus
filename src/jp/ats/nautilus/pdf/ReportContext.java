package jp.ats.nautilus.pdf;

import java.util.List;

import jp.ats.nautilus.common.U;

public class ReportContext {

	private static final ThreadLocal<String> fileHolder = new ThreadLocal<String>();

	private static final ThreadLocal<String> pageHolder = new ThreadLocal<String>();

	private static final ThreadLocal<String> recordHolder = new ThreadLocal<String>();

	private static final ThreadLocal<String> fieldHolder = new ThreadLocal<String>();

	public static void startFile(String file) {
		fileHolder.set(file);
	}

	public static void endFile() {
		fileHolder.set(null);
	}

	public static void startPage(int page) {
		pageHolder.set(String.valueOf(page));
	}

	public static void endPage() {
		pageHolder.set(null);
	}

	public static void startRecord(String record) {
		recordHolder.set(record);
	}

	public static void endRecord() {
		recordHolder.set(null);
	}

	public static void startField(String field) {
		fieldHolder.set(field);
	}

	public static void endField() {
		fieldHolder.set(null);
	}

	public static String getContextString() {
		List<String> buffer = U.newLinkedList();

		String file = fileHolder.get();
		if (U.isAvailable(file)) buffer.add("file:[" + file + "]");

		String page = pageHolder.get();
		if (U.isAvailable(page)) buffer.add("page:[" + page + "]");

		String record = recordHolder.get();
		if (U.isAvailable(record)) buffer.add("record:[" + record + "]");

		String field = fieldHolder.get();
		if (U.isAvailable(field)) buffer.add("field:[" + field + "]");

		return U.join(buffer, " ");
	}
}
