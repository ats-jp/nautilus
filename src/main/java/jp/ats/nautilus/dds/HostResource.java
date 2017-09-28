package jp.ats.nautilus.dds;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileInputStream;
import com.ibm.as400.access.QSYSObjectPathName;

import jp.ats.nautilus.common.U;

public class HostResource {

	public final int length;

	private final AS400 as400;

	private final QSYSObjectPathName name;

	public HostResource(AS400 as400, QSYSObjectPathName name, int length) {
		this.as400 = as400;
		this.name = name;
		this.length = length;
	}

	public InputStream open() {
		try {
			return new IFSFileInputStream(new IFSFile(as400, name.getPath()));
		} catch (IOException e) {
			throw new HostIOException(e);
		} catch (AS400SecurityException e) {
			throw new HostSecurityException(e);
		}
	}

	public void read(Consumer<byte[]> consumer) {
		byte[] buffer = new byte[length];
		try (InputStream input = U.wrap(open())) {
			int readed;
			while ((readed = input.read(buffer)) == length) {
				consumer.accept(buffer);
			}

			if (readed != -1)
				throw new IllegalStateException(Integer.toString(readed));
		} catch (IOException e) {
			throw new HostIOException(e);
		}
	}

	public HostData createAS400Data() {
		try (InputStream input = new IFSFileInputStream(
			new IFSFile(as400, name.getPath()))) {
			return new HostData(U.readBytes(input), length);
		} catch (IOException e) {
			throw new HostIOException(e);
		} catch (AS400SecurityException e) {
			throw new HostSecurityException(e);
		}
	}
}
