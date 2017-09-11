package jp.ats.nautilus.dds;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import jp.ats.nautilus.common.CP932;
import jp.ats.nautilus.common.CollectionMap;
import jp.ats.nautilus.common.U;
import jp.ats.nautilus.pdf.Nautilus;
import jp.ats.nautilus.pdf.ReportContext;
import jp.ats.nautilus.pdf.TemplateManager.Template;

public class SpoolFileReportLister extends ReportLister {

	private final int recordIndicatorPosition;

	private final SpoolPage[] pages;

	private final TemplateVendor templateVendor;

	private final IndicatorsVendor indicatorsVendor;

	private final UnderlineOnlyFieldVendor underlineOnlyFieldVendor;

	private final BarcodeFieldVendor barcodeFieldVendor;

	private final boolean strict;

	private DDSFile dds;

	private SpoolPage currentPage;

	public SpoolFileReportLister(
		String[] lines,
		int rows,
		int recordIndicatorPosition,
		TemplateVendor templateVendor,
		IndicatorsVendor indicatorsVendor,
		UnderlineOnlyFieldVendor underlineOnlyFieldVendor,
		BarcodeFieldVendor barcodeFieldVendor,
		boolean strict) {
		pages = convert(lines, rows);
		this.recordIndicatorPosition = recordIndicatorPosition;
		this.templateVendor = templateVendor;
		this.indicatorsVendor = indicatorsVendor;
		this.underlineOnlyFieldVendor = underlineOnlyFieldVendor;
		this.barcodeFieldVendor = barcodeFieldVendor;
		this.strict = strict;
	}

	@Override
	public void execute(DDSFile dds, Nautilus nautilus) {
		ReportContext.startFile(dds.getName());

		this.dds = dds;

		if (templateVendor != null) {
			for (Template template : templateVendor
				.getTemplates(dds.getName())) {
				nautilus.addTemplate(template);
			}
		}

		for (SpoolPage page : pages) {
			currentPage = page;
			executePage(dds, nautilus);
		}

		ReportContext.endFile();
	}

	@Override
	protected DDSRecord[] orderRecords(DDSRecord[] originalRecords) {
		return currentPage
			.orderRecords(originalRecords, recordIndicatorPosition);
	}

	@Override
	protected Indicators getIndicators(DDSRecord record) {
		if (indicatorsVendor == null) return null;
		return indicatorsVendor
			.getIndicators(dds.getName(), getCurrentRecord().getName());
	}

	@Override
	protected FieldDrawer[] createDrawers(DDSField[] matchingFields) {
		//行ごとにフィールドをまとめる
		CollectionMap<Integer, DDSField> map = CollectionMap.newInstance();
		for (DDSField field : matchingFields) {
			map.put(field.getRowIndex(), field);
		}

		List<FieldDrawer> drawers = U.newLinkedList();
		for (Integer rowIndex : map.keySet()) {
			Collection<DDSField> fields = map.get(rowIndex);

			int lineCount = currentPage.getLineCount(rowIndex - 1);
			if (lineCount > 1) {
				//スプールファイルの行が重複している場合
				drawers.addAll(
					convertToDrawers(
						fields,
						currentPage.getLines(rowIndex - 1)));
			} else {
				Collection<FieldDrawer> singleLineDrawers;
				if (strict) {
					singleLineDrawers = convertToDrawersStrict(
						fields,
						currentPage.getLine(rowIndex - 1));
				} else {
					singleLineDrawers = convertToDrawers(
						fields,
						currentPage.getLine(rowIndex - 1));
				}

				drawers.addAll(singleLineDrawers);
			}
		}

		return drawers.toArray(new FieldDrawer[drawers.size()]);
	}

	public void writePages(Path outputFile) throws IOException {
		PrintWriter writer = new PrintWriter(Files.newOutputStream(outputFile));
		int pageNumber = 0;
		for (SpoolPage page : pages) {
			writer.println("----- page " + ++pageNumber + " -----");

			int rows = page.getRows();
			for (int i = 0; i < rows; i++) {
				if (page.getLineCount(i) > 1) {
					String[] lines = page.getLines(i);
					writer.println("----- duplicate lines start -----");
					for (int ii = 0; ii < lines.length; ii++) {
						writer.println(lines[ii]);
					}
					writer.println("----- duplicate lines end -----");
				} else {
					writer.println(page.getLine(i));
				}
			}
		}

		writer.close();
	}

	static String treatYen(String string) {
		return string.replaceAll("\\u00A5", "\\\\");
	}

	private Collection<FieldDrawer> convertToDrawers(
		Collection<DDSField> fields,
		String[] lines) {
		List<FieldDrawer> drawers = U.newLinkedList();

		//条件完全反転比較のために一つ前のフィールドを確保
		DDSField prev = null;

		int currentIndex = 0;
		//定義順のフィールドのX位置が戻った回数をカウントする
		int count = 0;
		for (DDSField field : fields) {
			ReportContext.startField(field.getConvenientName());

			int index = field.getColumnIndex();

			if (index <= currentIndex) {
				OverlapCheckerElement prevElement = new OverlapCheckerElement(
					lines[count],
					prev);
				OverlapCheckerElement currentElement = new OverlapCheckerElement(
					lines[count],
					field);

				//戻った場合でも、隣接するフィールド同士が完全反転条件による切替だった場合
				//マージすることが可能となる
				if ((prevElement.isIncluding(currentElement)
					|| currentElement.isIncluding(prevElement))
					&& prev.isReverseCondition(field)) {

					//正しくマージするために前後を決めておく
					OverlapCheckerElement front, rear;
					if (prevElement.start <= currentElement.start) {
						front = prevElement;
						rear = currentElement;
					} else {
						front = currentElement;
						rear = prevElement;
					}

					front.merge(rear);

					drawers.add(front.create());

					currentIndex = front.end;

					prev = field;

					continue;
				}

				count++;
			}

			if (count >= lines.length) break;

			currentIndex = index + field.getLength() - 1;

			drawers.add(
				createFieldDrawer(
					field,
					getFieldValue(
						lines[count],
						field.getColumnIndex(),
						field.getLength()),
					field.getLength()));

			prev = field;
		}

		ReportContext.endField();

		//行の重複数とフィールドのX位置が戻った回数が一致しない場合
		//重複の解決に必要な情報が足りないのでエラーとする
		if (count != lines.length - 1) throw new IllegalStateException(
			"フィールド重複のため、スプールファイルの重複行から値を取り込めません。");

		return drawers;
	}

	private Collection<FieldDrawer> convertToDrawers(
		Collection<DDSField> fields,
		String line) {
		List<FieldDrawer> drawers = U.newLinkedList();
		for (DDSField field : fields) {
			ReportContext.startField(field.getConvenientName());

			drawers.add(
				createFieldDrawer(
					field,
					getFieldValue(
						line,
						field.getColumnIndex(),
						field.getLength()),
					field.getLength()));
		}

		ReportContext.endField();

		return drawers;
	}

	private Collection<FieldDrawer> convertToDrawersStrict(
		Collection<DDSField> fields,
		String line) {
		List<DDSField> sorted = new LinkedList<>(fields);

		Collections.sort(sorted, ColumnComparator.singleton);

		String recordName = getCurrentRecord().getName();

		List<FieldDrawer> drawers = U.newLinkedList();

		OverlapChecker checker = new OverlapChecker();

		String fileName = dds.getName();
		for (DDSField field : sorted) {
			ReportContext.startField(field.getConvenientName());

			if (underlineOnlyFieldVendor != null
				&& underlineOnlyFieldVendor
					.isUnderlineOnly(fileName, recordName, field.getName())) {
				//underlineOnly指定があるものは、オーバーラップチェックをせず
				//描画対象にする
				drawers.add(createFieldDrawer(field, "", field.getLength()));

				continue;
			}

			checker.add(new OverlapCheckerElement(line, field));
		}

		ReportContext.endField();

		checker.addAllTo(drawers);

		return drawers;
	}

	private FieldDrawer createFieldDrawer(
		DDSField field,
		String value,
		int length) {
		int startLine = field.getRowIndex();
		int startColumn = field.getStartColumn();

		DDSRecord record = field.getParent();
		if (barcodeFieldVendor != null) {
			String barcodeFactoryClass = barcodeFieldVendor
				.getBarcodeFactroyClass(
					record.getParent().getName(),
					record.getName(),
					field.getName());

			if (barcodeFactoryClass != null) return new BarcodeFieldDrawer(
				barcodeFactoryClass,
				value,
				startLine,
				startColumn);
		}

		return new TextFieldDrawer(
			value,
			startLine,
			startColumn,
			field.getCHRSIZ(),
			field.isUnderlined(),
			length);
	}

	private static class OverlapChecker {

		private final List<OverlapCheckerElement> elements = U.newLinkedList();

		private OverlapCheckerElement current;

		private void add(OverlapCheckerElement element) {
			if (current == null) {
				elements.add(element);
				current = element;
				return;
			}

			if (current.isOverlap(element)) {
				boolean isUnderlined = current.field.isUnderlined();
				if (!current.field.getCHRSIZ().equals(element.field.getCHRSIZ())
					|| isUnderlined != element.field.isUnderlined()) {
					throw new OverlapException(current.field, element.field);
				}

				//一方がもう一方を内包状態で、文字修飾が同じ場合、フィールドを結合
				//長さが違う項目で下線がある場合、桁数の長い方に合わせるので
				//元帳票とちがって下線が長く出てしまうが、ひとまず良しとする
				//または、一部分だけがクロスした状態で、文字サイズが同じ、
				//下線が双方ない場合、フィールドを結合
				//この場合は、結合しても見た目を変えないと思われる
				if (current.isIncluding(element)
					//上で下線の状態が同じか判定済なので、ここでは片方のみ判定
					|| !isUnderlined) {
					current.merge(element);
				} else {
					//そうでなければエラーとする
					throw new OverlapException(current.field, element.field);
				}
			} else {
				elements.add(element);
				current = element;
			}
		}

		private void addAllTo(List<FieldDrawer> drawers) {
			for (OverlapCheckerElement element : elements) {
				drawers.add(element.create());
			}

			ReportContext.endField();
		}
	}

	private class OverlapCheckerElement {

		private final String line;

		private final DDSField field;

		private int start;

		private int end;

		private OverlapCheckerElement(String line, DDSField field) {
			this.line = line;
			this.field = field;
			//オーバーラップ判定には、値取得開始位置(columnIndex)と文字数を使用する
			//描画開始位置(startColumn)は使用しない
			//オーバーラップが問題になるのは、あくまでも値の取得が重なる場合であり、
			//描画は意図して重ねる可能性もあるからである
			start = field.getColumnIndex() - 1;
			end = start + field.getLength();
		}

		private boolean isOverlap(OverlapCheckerElement next) {
			return end > next.start;
		}

		private boolean isIncluding(OverlapCheckerElement next) {
			return end >= next.end;
		}

		private void merge(OverlapCheckerElement next) {
			if (end < next.end) end = next.end;
		}

		private FieldDrawer create() {
			int length = end - start;

			ReportContext.startField(field.getConvenientName());

			return createFieldDrawer(
				field,
				getFieldValue(line, field.getColumnIndex(), length),
				length);
		}
	}

	private static class ColumnComparator implements Comparator<DDSField> {

		private static final ColumnComparator singleton = new ColumnComparator();

		@Override
		public int compare(DDSField o1, DDSField o2) {
			int result = o1.getColumnIndex() - o2.getColumnIndex();
			//同じ開始位置だった場合、長いほうが前へ
			return result != 0 ? result : o2.getLength() - o1.getLength();
		}
	}

	private static String getFieldValue(
		String line,
		int columnIndex,
		int length) {
		byte[] bytes = CP932.treatForCP932(line)
			.getBytes(ReportLister.MEASURE_CHARSET);

		byte[] dest = new byte[length];

		System.arraycopy(bytes, columnIndex - 1, dest, 0, length);

		return new String(dest, ReportLister.MEASURE_CHARSET);
	}

	private static SpoolPage[] convert(String[] lines, int rows) {
		List<SpoolPage> pages = U.newLinkedList();

		int columns = getColumns(lines);

		SpoolPage page = new SpoolPage(rows, columns);

		for (String line : lines) {
			if (page.adoptLine(line)) continue;

			//改ページ発生
			pages.add(page);
			page = new SpoolPage(rows, columns);
			page.adoptLine(line);
		}

		pages.add(page);

		return pages.toArray(new SpoolPage[pages.size()]);
	}

	private static int getColumns(String[] lines) {
		return Arrays.asList(lines)
			.stream()
			.filter(line -> line != null)
			.findFirst()
			.orElseThrow(() -> new NullPointerException())
			.getBytes(ReportLister.MEASURE_CHARSET).length;
	}
}
