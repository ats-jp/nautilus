package jp.ats.nautilus.dds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.ibm.as400.access.AS400;

import jp.ats.nautilus.common.U;
import jp.ats.nautilus.dds.Controller.Result;
import jp.ats.nautilus.pdf.Rectangle;
import jp.ats.nautilus.pdf.ReportContext;

public class DynamicController {

	private final Path debugRepository;

	public DynamicController() {
		debugRepository = null;
	}

	public DynamicController(Path debugRepository) {
		this.debugRepository = debugRepository;
	}

	public Path execute(
		String pdfName,
		String ddsName,
		String spoolName,
		AS400 as400,
		String fontPath,
		Path dynamicConfigures,
		Path outputDirectory,
		Path templateDirectory,
		Path templates,
		Path indicators,
		Path underlineOnlyFields,
		Path barcodeFields,
		boolean strict)
		throws InterruptedException, IOException {
		Configure configure = null;
		for (String line : Files.readAllLines(dynamicConfigures)) {
			if (!U.presents(line)) continue;

			String[] elements = line.split("\\s");
			if (ddsName.equals(elements[0])) {
				configure = new Configure(elements);
				break;
			}
		}

		if (configure == null)
			throw new IllegalStateException("設定ファイル内に " + ddsName + " が存在しません");

		HostData dds, spool;

		dds = HostUtilities
			.readAS400Source(
				as400,
				configure.ddsLib,
				configure.ddsFile,
				ddsName)
			.createAS400Data();

		spool = HostUtilities
			.readAS400File(
				as400,
				configure.spoolLib,
				configure.spoolFile,
				spoolName)
			.createAS400Data();

		Result result = new Controller(debugRepository).execute(
			pdfName,
			ddsName,
			dds,
			configure.rows,
			configure.columns,
			configure.lpi,
			configure.cpi,
			configure.marginLeftMM,
			configure.marginTopMM,
			configure.recordIndicatorPosition,
			configure.rectangle,
			configure.autoAdjust,
			spool,
			fontPath,
			outputDirectory,
			templateDirectory,
			templates,
			indicators,
			underlineOnlyFields,
			barcodeFields,
			strict);

		ReportContext.endField();
		ReportContext.endRecord();
		ReportContext.endPage();
		ReportContext.endFile();

		return result.pdf;
	}

	private static class Configure {

		private final String ddsLib;

		private final String ddsFile;

		private final int rows;

		private final int columns;

		private final float lpi;

		private final float cpi;

		private final int marginLeftMM;

		private final int marginTopMM;

		private final int recordIndicatorPosition;

		private final Rectangle rectangle;

		private final boolean autoAdjust;

		private final String spoolLib;

		private final String spoolFile;

		private Configure(String[] elements) {
			ddsLib = elements[1];
			ddsFile = elements[2];
			rows = Integer.parseInt(elements[3]);
			columns = Integer.parseInt(elements[4]);
			lpi = Float.parseFloat(elements[5]);
			cpi = Float.parseFloat(elements[6]);
			marginLeftMM = Integer.parseInt(elements[7]);
			marginTopMM = Integer.parseInt(elements[8]);
			recordIndicatorPosition = Integer.parseInt(elements[9]);
			rectangle = Rectangle.valueOf(elements[10]);
			autoAdjust = Boolean.valueOf(elements[11]);
			spoolLib = elements[12];
			spoolFile = elements[13];
		}
	}
}
