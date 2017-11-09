package jp.ats.nautilus.dds;

import java.beans.PropertyVetoException;

import com.ibm.as400.access.AS400;

public class HostHandler {

	private final String address;

	private final String user;

	private final String password;

	private final String spoolLib;

	private final String spoolFile;

	private final String spoolMember;

	public HostHandler(
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

	public HostResource getResource() throws InterruptedException {
		return HostUtilities
			.readAS400File(create(), spoolLib, spoolFile, spoolMember);
	}

	private AS400 create() {
		AS400 as400 = new AS400(address, user, password);
		try {
			as400.setGuiAvailable(false);
		} catch (PropertyVetoException e) {
			throw new IllegalStateException(e);
		}

		return as400;
	}
}
