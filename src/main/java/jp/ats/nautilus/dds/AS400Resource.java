package jp.ats.nautilus.dds;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileInputStream;
import com.ibm.as400.access.QSYSObjectPathName;

import jp.ats.nautilus.common.U;

public class AS400Resource {

	public final int length;

	private final AS400 as400;

	private final QSYSObjectPathName name;

	public AS400Resource(AS400 as400, QSYSObjectPathName name, int length) {
		this.as400 = as400;
		this.name = name;
		this.length = length;
	}

	public InputStream open() throws IOException, AS400SecurityException {
		return new IFSFileInputStream(new IFSFile(as400, name.getPath()));
	}

	public void read(Consumer<byte[]> consumer)
		throws IOException, AS400SecurityException {
		byte[] buffer = new byte[length];
		try (InputStream input = new BufferedInputStream(open())) {
			int readed;
			while ((readed = input.read(buffer)) == length) {
				consumer.accept(buffer);
			}

			if (readed != -1)
				throw new IllegalStateException(Integer.toString(readed));
		}
	}

	public AS400Data createAS400Data()
		throws IOException, AS400SecurityException {
		try (InputStream input = new IFSFileInputStream(
			new IFSFile(as400, name.getPath()))) {
			return new AS400Data(U.readBytes(input), length);
		}
	}
}
