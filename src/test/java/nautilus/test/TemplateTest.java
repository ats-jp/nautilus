package nautilus.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class TemplateTest {

	public static void main(String[] args) throws Exception {
		//PDFMergerUtility merger = new PDFMergerUtility();

		Path path = getDesktopPath().resolve("ok18897.pdf");

		PDDocument template = PDDocument.load(Files.newInputStream(path));

		PDPage page = template.getDocumentCatalog().getPages().get(2);

		//clone test
		COSDictionary pageDict = page.getCOSObject();
		COSDictionary newPageDict = new COSDictionary(pageDict);
		newPageDict.removeItem(COSName.ANNOTS);
		PDPage newPage = new PDPage(newPageDict);

		PDDocument document = new PDDocument();
		document.addPage(newPage);

		PDPageContentStream contentStream = new PDPageContentStream(
			document,
			newPage,
			AppendMode.APPEND,
			true);

		contentStream.setFont(PDType1Font.TIMES_ROMAN, 46);
		contentStream.beginText();
		contentStream.showText("Hello world!");
		contentStream.endText();

		contentStream.close();

		//		ByteArrayOutputStream out = new ByteArrayOutputStream();
		document.save(
			Files.newOutputStream(getDesktopPath().resolve("new18897.pdf")));
		//		merger.addSource(new ByteArrayInputStream(out.toByteArray()));
		//		document.close();
		//
		//		merger.setDestinationStream(
		//			Files.newOutputStream(getDesktopPath().resolve("new18897.pdf")));
		//
		//		merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
	}

	public static Path getDesktopPath() {
		return Paths.get(System.getProperty("user.home"), "Desktop");
	}

}
