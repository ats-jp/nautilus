package jp.ats.nautilus.dds;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import com.lowagie.text.DocumentException;

import jp.ats.nautilus.common.U;
import jp.ats.nautilus.pdf.Nautilus;
import jp.ats.nautilus.pdf.PageSize;
import jp.ats.nautilus.pdf.ReportContext;

public class Controller {

	private static final AtomicLong counter = new AtomicLong(0);

	private final Path debugRepository;

	public Controller() {
		debugRepository = null;
	}

	public Controller(Path debugRepository) {
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
		Path outputDirectory,
		Path templateDirectory,
		Path templates,
		Path indicators,
		Path underlineOnlyFields,
		Path barcodeFields,
		boolean strict)
		throws IOException {
		ConcreteFontManager.setFontDirectory(fontDirectory);

		Path saveDirectory = null;

		boolean debugMode = debugRepository != null
			&& Files.exists(debugRepository)
			&& Files.isDirectory(debugRepository);

		if (debugMode) {
			saveDirectory = debugRepository.resolve(
				U.formatDate("yyyyMMddHHmmss", new Date())
					+ "."
					+ ddsName
					+ "."
					+ counter.getAndAdd(1));

			Files.createDirectories(saveDirectory);

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
				outputDirectory.toAbsolutePath().toString());
			properties.setProperty(
				"templateDirectory",
				templateDirectory.toAbsolutePath().toString());
			properties.setProperty(
				"templates",
				templates.toAbsolutePath().toString());
			properties.setProperty(
				"indicators",
				indicators.toAbsolutePath().toString());
			properties.setProperty(
				"underlineOnlyFields",
				underlineOnlyFields.toAbsolutePath().toString());
			properties.setProperty(
				"barcodeFields",
				barcodeFields.toAbsolutePath().toString());
			properties.setProperty("strict", String.valueOf(strict));

			properties.setProperty(
				"ddsLineLength",
				String.valueOf(ddsData.getLineLength()));
			properties.setProperty(
				"spoolLineLength",
				String.valueOf(spoolData.getLineLength()));

			try (OutputStream output = Files
				.newOutputStream(saveDirectory.resolve("properties.xml"))) {
				properties.storeToXML(output, null, "UTF-8");
				output.flush();
			}

			write(ddsData.getData(), saveDirectory.resolve("dds.original"));
			write(spoolData.getData(), saveDirectory.resolve("spool.original"));
		}

		IndicatorsVendor indicatorsVendor = new IndicatorsVendor(indicators);

		TemplateVendor templateVendor = new TemplateVendor(
			templates,
			templateDirectory);

		UnderlineOnlyFieldVendor underlineOnlyFieldVendor = new UnderlineOnlyFieldVendor(
			underlineOnlyFields);

		BarcodeFieldVendor barcodeFieldVendor = new BarcodeFieldVendor(
			barcodeFields);

		Path pdf;
		try {
			String[] ddsLines = AS400Utilities.convert(ddsData.read(), false);

			if (debugMode) write(ddsLines, saveDirectory.resolve("dds.txt"));

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
				write(spoolLines, saveDirectory.resolve("spool.txt"));

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
				lister.writePages(saveDirectory.resolve("expand.txt"));

			lister.execute(dds, nautilus);

			if (debugMode) {
				OutputStream output = Files
					.newOutputStream(saveDirectory.resolve("nautilus.xml"));
				nautilus.save(output);
				output.close();
			}

			pdf = outputDirectory.resolve(pdfName + ".pdf");
			int counter = 0;
			while (Files.exists(pdf)) {
				//同名ファイルが既にある場合、名前を変更
				pdf = outputDirectory
					.resolve(pdfName + "-" + (++counter) + ".pdf");
			}

			OutputStream output = Files.newOutputStream(pdf);

			try {
				nautilus.draw(output);
			} finally {
				U.close(output);
			}
		} catch (Exception e) {
			if (debugMode) {
				PrintWriter writer = new PrintWriter(
					Files.newOutputStream(saveDirectory.resolve("error.log")));
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
			Paths.get(U.care(properties.getProperty("outputDirectory"))),
			Paths.get(U.care(properties.getProperty("templateDirectory"))),
			Paths.get(U.care(properties.getProperty("templates"))),
			Paths.get(U.care(properties.getProperty("indicators"))),
			Paths.get(U.care(properties.getProperty("underlineOnlyFields"))),
			Paths.get(U.care(properties.getProperty("barcodeFields"))),
			Boolean.parseBoolean(properties.getProperty("strict")));
	}

	public static class Result {

		public final Path pdf;

		public final Path saveDirectory;

		private Result(Path pdf, Path saveDirectory) {
			this.pdf = pdf;
			this.saveDirectory = saveDirectory;
		}
	}

	private static void write(byte[] input, Path outputFile)
		throws IOException {
		BufferedOutputStream output = new BufferedOutputStream(
			Files.newOutputStream(outputFile));

		U.sendBytes(new ByteArrayInputStream(input), output);

		output.flush();
		output.close();
	}

	private static void write(String[] lines, Path outputFile)
		throws IOException {
		PrintWriter writer = new PrintWriter(Files.newOutputStream(outputFile));

		for (String line : lines) {
			writer.println(line);
		}

		writer.flush();
		writer.close();
	}
}
