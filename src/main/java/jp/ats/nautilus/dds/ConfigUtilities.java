package jp.ats.nautilus.dds;

import java.io.File;
import java.io.IOException;

import com.ibm.as400.access.AS400;

import jp.ats.nautilus.common.TextReader;
import jp.ats.nautilus.common.U;

public class ConfigUtilities {

	public static File readOutputConfig(File outputConfig) throws IOException {
		if (!outputConfig.exists()) return null;

		String[] lines = new TextReader(outputConfig.toURI().toURL())
			.readLinesAsArray();

		if (lines.length == 0) return null;

		String line = lines[0].trim();

		if (!U.isAvailable(line)) return null;

		File outputDirecotory = new File(line);
		if (!outputDirecotory.exists()) throw new IllegalStateException(
			outputDirecotory.getAbsolutePath() + " は存在しません。");

		return outputDirecotory;
	}

	public static AS400 createAS400Object(File as400Config) throws IOException {
		if (!as400Config.exists()) return null;

		String[] lines = new TextReader(as400Config.toURI().toURL())
			.readLinesAsArray();

		if (lines.length == 0) return null;

		String line = lines[0].trim();

		if (!U.isAvailable(line)) return null;

		String[] columns = line.split("\\t");

		return new AS400(columns[0], columns[1], columns[2]);
	}

}
