package jp.ats.nautilus.dds;

public class HostException extends RuntimeException {

	private static final long serialVersionUID = -4947731355397408708L;

	HostException(Exception e) {
		super(e);
	}
}
