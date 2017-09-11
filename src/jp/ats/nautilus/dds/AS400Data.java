package jp.ats.nautilus.dds;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jp.ats.nautilus.common.U;

public class AS400Data {

	private final byte[] data;

	private final int lineLength;

	public AS400Data(byte[] data, int lineLength) throws IOException {
		this.data = data;
		this.lineLength = lineLength;
	}

	public byte[] getData() {
		return data;
	}

	public int getLineLength() {
		return lineLength;
	}

	public byte[][] read() throws IOException {
		InputStream input = new ByteArrayInputStream(data);

		List<byte[]> list = U.newLinkedList();
		try {
			int readed = 0;
			do {
				byte[] buffer = new byte[lineLength];
				readed = input.read(buffer);
				list.add(buffer);
			} while (readed > 0);
		} finally {
			U.close(input);
		}

		return list.toArray(new byte[list.size()][]);
	}
}
