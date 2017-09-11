package jp.ats.nautilus.dds;

import java.beans.PropertyVetoException;
import java.io.IOException;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;

public class AS400Handler {

	private final String address;

	private final String user;

	private final String password;

	private final String spoolLib;

	private final String spoolFile;

	private final String spoolMember;

	public AS400Handler(
		String address,
		String user,
		String password,
		String spoolLib,
		String spoolFile,
		String spoolMember) {
		this.address = address;
		this.user = user;
		this.password = password;
		this.spoolLib = spoolLib;
		this.spoolFile = spoolFile;
		this.spoolMember = spoolMember;
	}

	public AS400 connect() {
		return new AS400(address, user, password);
	}

	public AS400Resource getResource() throws InterruptedException {
		try {
			return AS400Utilities
				.readAS400File(connect(), spoolLib, spoolFile, spoolMember);
		} catch (IOException | AS400Exception | AS400SecurityException
			| PropertyVetoException e) {
			throw new IllegalStateException(e);
		}
	}
}
