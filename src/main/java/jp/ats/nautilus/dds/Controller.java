package jp.ats.nautilus.dds;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import com.itextpdf.text.DocumentException;

import jp.ats.nautilus.common.U;
import jp.ats.nautilus.pdf.Nautilus;
import jp.ats.nautilus.pdf.PageSize;
import jp.ats.nautilus.pdf.ReportContext;

public class Controller {

	private static final AtomicLong counter = new AtomicLong(0);

	private final File debugRepository;

	public Controller() {
		debugRepository = null;
	}

	public Controller(File debugRepository) {
		this.debugRepository = debugRepository;
	}

	/**
	 * @param pdfName 作成されるPDFの名前
	 * @param ddsName DDS名
	 * @param ddsData DDS生データ
	 * @param rows 行数
	 * @param columns 桁数
	 * @param lpi LPI
	 * @param cpi CPI
	 * @param marginLeftMM 左マージン(mm)
	 * @param marginTopMM 上マージン(mm)
	 * @param recordIndicatorPosition スプール内のレコード指示コード位置
	 * @param page 出力形式
	 * @param spoolData スプール生データ
	 * @param fontPath 拡張フォント置場
	 * @param outputDirectory PDF出力場所
	 * @param templateDirectory テンプレートPDF置場
	 * @param templates テンプレート設定ファイル
	 * @param indicators オプション標識設定ファイル
	 * @param underlineOnlyFields アンダーラインのみフィールド設定ファイル
	 * @param barcodeFields バーコードフィールド設定ファイル
	 * @throws IOException
	 */
	public Result execute(
		String pdfName,
		String ddsName,
		AS400Data ddsData,
		int rows,
		int columns,
		float lpi,
		float cpi,
		int marginLeftMM,
		int marginTopMM,
		int recordIndicatorPosition,
		PageSize pageSize,
		AS400Data spoolData,
		String fontDirectory,
		File outputDirectory,
		File templateDirectory,
		File templates,
		File indicators,
		File underlineOnlyFields,
		File barcodeFields,
		boolean strict) throws IOException {
		ConcreteFontManager.setFontDirectory(fontDirectory);

		File saveDirectory = null;

		boolean debugMode = debugRepository != null
			&& debugRepository.exists()
			&& debugRepository.isDirectory();

		if (debugMode) {
			saveDirectory = new File(
				debugRepository,
				U.formatDate("yyyyMMddHHmmss", new Date())
					+ "."
					+ ddsName
					+ "."
					+ counter.getAndAdd(1));

			if (!saveDirectory.mkdirs())
				throw new IllegalStateException(saveDirectory + " が作成できません");

			Properties properties = new Properties();
			properties.setProperty("pdfName", pdfName);
			properties.setProperty("ddsName", ddsName);
			properties.setProperty("rows", String.valueOf(rows));
			properties.setProperty("columns", String.valueOf(columns));
			properties.setProperty("lpi", String.valueOf(lpi));
			properties.setProperty("cpi", String.valueOf(cpi));
			properties
				.setProperty("marginLeftMM", String.valueOf(marginLeftMM));
			properties.setProperty("marginTopMM", String.valueOf(marginTopMM));
			properties.setProperty(
				"recordIndicatorPosition",
				String.valueOf(recordIndicatorPosition));
			properties.setProperty("pageSize", pageSize.name());
			properties.setProperty("fontDirectory", fontDirectory);
			properties.setProperty(
				"outputDirectory",
				outputDirectory.getAbsolutePath());
			properties.setProperty(
				"templateDirectory",
				templateDirectory.getAbsolutePath());
			properties.setProperty("templates", templates.getAbsolutePath());
			properties.setProperty("indicators", indicators.getAbsolutePath());
			properties.setProperty(
				"underlineOnlyFields",
				underlineOnlyFields.getAbsolutePath());
			properties
				.setProperty("barcodeFields", barcodeFields.getAbsolutePath());
			properties.setProperty("strict", String.valueOf(strict));

			properties.setProperty(
				"ddsLineLength",
				String.valueOf(ddsData.getLineLength()));
			properties.setProperty(
				"spoolLineLength",
				String.valueOf(spoolData.getLineLength()));

			FileOutputStream output = new FileOutputStream(
				new File(saveDirectory, "properties.xml"));

			try {
				properties.storeToXML(output, null, "UTF-8");
			} finally {
				output.flush();
				output.close();
			}

			write(ddsData.getData(), new File(saveDirectory, "dds.original"));
			write(
				spoolData.getData(),
				new File(saveDirectory, "spool.original"));
		}

		IndicatorsVendor indicatorsVendor = new IndicatorsVendor(indicators);

		TemplateVendor templateVendor = new TemplateVendor(
			templates,
			templateDirectory);

		UnderlineOnlyFieldVendor underlineOnlyFieldVendor = new UnderlineOnlyFieldVendor(
			underlineOnlyFields);

		BarcodeFieldVendor barcodeFieldVendor = new BarcodeFieldVendor(
			barcodeFields);

		File pdf;
		try {
			String[] ddsLines = AS400Utilities.convert(ddsData.read(), false);

			if (debugMode) write(ddsLines, new File(saveDirectory, "dds.txt"));

			DDSFile dds = DDSFile.getInstance(ddsName, ddsLines);

			Nautilus nautilus = new Nautilus();

			nautilus.setRows(rows);
			nautilus.setColumns(columns);
			nautilus.setLPI(lpi);
			nautilus.setCPI(cpi);
			nautilus.setMarginLeft(marginLeftMM);
			nautilus.setMarginTop(marginTopMM);
			nautilus.setPageSize(pageSize);
			nautilus.setFontManagerClass(ConcreteFontManager.class);

			String[] spoolLines = AS400Utilities
				.convert(ZeroLineConcatenator.execute(spoolData.read()), true);

			if (debugMode)
				write(spoolLines, new File(saveDirectory, "spool.txt"));

			SpoolFileReportLister lister = new SpoolFileReportLister(
				spoolLines,
				rows,
				recordIndicatorPosition,
				templateVendor,
				indicatorsVendor,
				underlineOnlyFieldVendor,
				barcodeFieldVendor,
				strict);

			if (debugMode)
				lister.writePages(new File(saveDirectory, "expand.txt"));

			lister.execute(dds, nautilus);

			if (debugMode) {
				OutputStream output = new FileOutputStream(
					new File(saveDirectory, "nautilus.xml"));
				nautilus.save(output);
				output.close();
			}

			pdf = new File(outputDirectory, pdfName + ".pdf");
			int counter = 0;
			while (pdf.exists()) {
				//同名ファイルが既にある場合、名前を変更
				pdf = new File(
					outputDirectory,
					pdfName + "-" + (++counter) + ".pdf");
			}

			OutputStream output = new FileOutputStream(pdf);

			try {
				nautilus.draw(output);
			} finally {
				U.close(output);
			}
		} catch (Exception e) {
			if (debugMode) {
				PrintWriter writer = new PrintWriter(
					new FileOutputStream(new File(saveDirectory, "error.log")));
				writer.println(ReportContext.getContextString());
				e.printStackTrace(writer);
				writer.flush();
				writer.close();
			}

			throw e;
		}

		return new Result(pdf, saveDirectory);
	}

	public void execute(File saveDirectory)
		throws IOException, DocumentException {
		FileInputStream propertiesInput = new FileInputStream(
			new File(saveDirectory, "properties.xml"));

		Properties properties = new Properties();

		properties.loadFromXML(propertiesInput);

		propertiesInput.close();

		FileInputStream ddsInput = new FileInputStream(
			new File(saveDirectory, "dds.original"));
		AS400Data dds = new AS400Data(
			U.readBytes(ddsInput),
			Integer.parseInt(properties.getProperty("ddsLineLength")));
		ddsInput.close();

		FileInputStream spoolInput = new FileInputStream(
			new File(saveDirectory, "spool.original"));
		AS400Data spool = new AS400Data(
			U.readBytes(spoolInput),
			Integer.parseInt(properties.getProperty("spoolLineLength")));
		spoolInput.close();

		execute(
			properties.getProperty("pdfName"),
			properties.getProperty("ddsName"),
			dds,
			Integer.parseInt(properties.getProperty("rows")),
			Integer.parseInt(properties.getProperty("columns")),
			Float.parseFloat(properties.getProperty("lpi")),
			Float.parseFloat(properties.getProperty("cpi")),
			Integer.parseInt(properties.getProperty("marginLeftMM")),
			Integer.parseInt(properties.getProperty("marginTopMM")),
			Integer.parseInt(properties.getProperty("recordIndicatorPosition")),
			PageSize.valueOf(properties.getProperty("pageSize")),
			spool,
			U.care(properties.getProperty("fontPath")),
			new File(U.care(properties.getProperty("outputDirectory"))),
			new File(U.care(properties.getProperty("templateDirectory"))),
			new File(U.care(properties.getProperty("templates"))),
			new File(U.care(properties.getProperty("indicators"))),
			new File(U.care(properties.getProperty("underlineOnlyFields"))),
			new File(U.care(properties.getProperty("barcodeFields"))),
			Boolean.parseBoolean(properties.getProperty("strict")));
	}

	public static class Result {

		public final File pdf;

		public final File saveDirectory;

		private Result(File pdf, File saveDirectory) {
			this.pdf = pdf;
			this.saveDirectory = saveDirectory;
		}
	}

	private static void write(byte[] input, File outputFile)
		throws IOException {
		BufferedOutputStream output = new BufferedOutputStream(
			new FileOutputStream(outputFile));

		U.sendBytes(new ByteArrayInputStream(input), output);

		output.flush();
		output.close();
	}

	private static void write(String[] lines, File outputFile)
		throws IOException {
		PrintWriter writer = new PrintWriter(new FileOutputStream(outputFile));

		for (String line : lines) {
			writer.println(line);
		}

		writer.flush();
		writer.close();
	}
}
