package jp.ats.nautilus.dds;

import java.nio.charset.Charset;
import java.util.List;

import jp.ats.nautilus.common.U;
import jp.ats.nautilus.pdf.Nautilus;
import jp.ats.nautilus.pdf.Page;
import jp.ats.nautilus.pdf.ReportContext;

public abstract class ReportLister {

	public static final Charset MEASURE_CHARSET = Charset.forName("MS932");

	private int currentPageNumber;

	private DDSRecord currentRecord;

	public abstract void execute(DDSFile dds, Nautilus nautilus);

	protected abstract DDSRecord[] orderRecords(DDSRecord[] originalRecords);

	protected abstract Indicators getIndicators(DDSRecord record);

	protected abstract FieldDrawer[] createDrawers(DDSField[] matchingFields);

	protected DDSRecord getCurrentRecord() {
		return currentRecord;
	}

	protected void executePage(DDSFile dds, Nautilus nautilus) {
		ReportContext.startPage(++currentPageNumber);

		nautilus.newPage();

		DDSRecord[] ordered = orderRecords(dds.getRecords());

		int currentRowIndex = 0;
		for (DDSRecord record : ordered) {
			ReportContext.startRecord(record.getName());

			currentRecord = record;

			List<DDSField> matchingFields = U.newLinkedList();
			currentRowIndex = record.adjustPositionForFields(
				matchingFields,
				getIndicators(record),
				currentRowIndex);

			executeRecord(
				record.getDFNLINs(),
				matchingFields.toArray(new DDSField[matchingFields.size()]),
				nautilus.currentPage());
		}

		ReportContext.endRecord();

		ReportContext.endPage();
	}

	private void executeRecord(DFNLIN[] dfnlins, DDSField[] fields, Page page) {
		for (DFNLIN dfnlin : dfnlins) {
			dfnlin.add(page);
		}

		FieldDrawer[] drawers = createDrawers(fields);

		for (FieldDrawer drawer : drawers) {
			drawer.draw(page);
		}
	}
}
