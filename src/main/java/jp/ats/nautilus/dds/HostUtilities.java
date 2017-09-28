package jp.ats.nautilus.dds;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.FieldDescription;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.SequentialFile;

import jp.ats.nautilus.common.CP932;
import jp.ats.nautilus.common.U;

public class HostUtilities {

	public static final Charset CHARSET = Charset.forName("CP930");

	private static final byte shiftIn = 14;

	private static final byte shiftOut = 15;

	public static HostResource readAS400File(
		AS400 as400,
		String lib,
		String file,
		String member)
		throws InterruptedException {
		QSYSObjectPathName name = new QSYSObjectPathName(
			lib,
			file,
			member,
			"MBR");

		SequentialFile sequentialFile = new SequentialFile(
			as400,
			name.getPath());

		try {
			sequentialFile.setRecordFormat();
		} catch (AS400SecurityException e) {
			throw new HostSecurityException(e);
		} catch (IOException e) {
			throw new HostIOException(e);
		} catch (AS400Exception | PropertyVetoException e) {
			throw new HostException(e);
		}

		RecordFormat format = sequentialFile.getRecordFormat();

		int length = 0;
		for (FieldDescription description : format.getFieldDescriptions()) {
			length += description.getLength();
		}

		return new HostResource(as400, name, length);
	}

	public static HostResource readAS400Source(
		AS400 as400,
		String lib,
		String file,
		String member)
		throws InterruptedException {
		QSYSObjectPathName name = new QSYSObjectPathName(
			lib,
			file,
			member,
			"MBR");

		SequentialFile sequentialFile = new SequentialFile(
			as400,
			name.getPath());

		try {
			sequentialFile.setRecordFormat();
		} catch (AS400SecurityException e) {
			throw new HostSecurityException(e);
		} catch (IOException e) {
			throw new HostIOException(e);
		} catch (AS400Exception | PropertyVetoException e) {
			throw new HostException(e);
		}

		RecordFormat format = sequentialFile.getRecordFormat();

		int length = format.getFieldDescription(format.getNumberOfFields() - 1)
			.getLength();

		return new HostResource(as400, name, length);
	}

	public static String addsSpacesAsShiftChars(String original) {
		return addsSpacesAsShiftChars(original.getBytes(CHARSET));
	}

	public static String addsSpacesAsShiftChars(byte[] as400Data) {
		StringBuilder builder = new StringBuilder();
		int index = 0;
		for (int i = 0; i < as400Data.length; i++) {
			int length = i - index;
			byte[] buffer;
			switch (as400Data[i]) {
			case shiftIn:
				buffer = new byte[length];
				System.arraycopy(as400Data, index, buffer, 0, length);
				builder.append(new String(buffer, CHARSET));
				index = i;
				break;

			case shiftOut:
				buffer = new byte[length];
				System.arraycopy(as400Data, index, buffer, 0, length);
				builder.append(' ');
				builder.append(new String(buffer, CHARSET));
				builder.append(' ');

				//1byte進めてshiftOutは飛ばす
				index = ++i;

				break;
			}
		}

		byte[] buffer = new byte[as400Data.length - index];
		System.arraycopy(as400Data, index, buffer, 0, buffer.length);

		builder.append(new String(buffer, CHARSET));

		return builder.toString();
	}

	public static String[] convert(
		byte[][] as400Data,
		boolean addsSpacesAsShiftChars) {
		List<String> lines = U.newLinkedList();
		for (byte[] line : as400Data) {
			String lineString = convertLine(line, addsSpacesAsShiftChars);
			if (lineString != null) lines.add(lineString);
		}

		return lines.toArray(new String[lines.size()]);
	}

	public static String[] convert(
		InputStream input,
		int length,
		boolean addsShiftChars) {
		try {
			return convert(
				new HostData(U.readBytes(input), length).read(),
				addsShiftChars);
		} catch (IOException e) {
			throw new HostIOException(e);
		}
	}

	public static void write(
		HostResource resource,
		PrintWriter writer,
		boolean addsShiftChars) {
		byte[] buffer = new byte[resource.length];
		try (InputStream input = U.wrap(resource.open())) {
			int readed;
			while ((readed = input.read(buffer)) == resource.length) {
				String lineString = convertLine(buffer, addsShiftChars);
				writer.println(CP932.treatForCP932(lineString));
			}

			if (readed != -1)
				throw new IllegalStateException(Integer.toString(readed));
		} catch (IOException e) {
			throw new HostIOException(e);
		}
	}

	public static void write(
		HostResource resource,
		Consumer<String> lineConsumer,
		boolean addsShiftChars) {
		byte[] buffer = new byte[resource.length];
		try (InputStream input = U.wrap(resource.open())) {
			int readed;
			while ((readed = input.read(buffer)) == resource.length) {
				String lineString = convertLine(buffer, addsShiftChars);
				lineConsumer.accept(CP932.treatForCP932(lineString));
			}

			if (readed != -1)
				throw new IllegalStateException(Integer.toString(readed));
		} catch (IOException e) {
			throw new HostIOException(e);
		}
	}

	public static String convertLine(
		byte[] line,
		boolean addsSpacesAsShiftChars) {
		String lineString;
		if (addsSpacesAsShiftChars) {
			lineString = addsSpacesAsShiftChars(line);
		} else {
			lineString = new String(line, CHARSET);
		}

		if (Pattern.matches("^\\x00+$", lineString)) return null;

		return lineString;
	}
}
