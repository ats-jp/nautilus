package jp.ats.nautilus.dds;

import com.ibm.as400.access.AS400SecurityException;

public class HostSecurityException extends HostException {

	private static final long serialVersionUID = 6252916608050899049L;

	HostSecurityException(AS400SecurityException e) {
		super(e);
	}
}
