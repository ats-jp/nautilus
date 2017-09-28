package jp.ats.nautilus.dds;

import java.io.IOException;

public class HostIOException extends HostException {

	private static final long serialVersionUID = 439812519149099775L;

	HostIOException(IOException e) {
		super(e);
	}
}
