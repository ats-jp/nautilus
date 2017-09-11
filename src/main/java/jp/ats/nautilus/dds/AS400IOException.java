package jp.ats.nautilus.dds;

@SuppressWarnings("serial")
public class AS400IOException extends RuntimeException {

	public AS400IOException(Exception e) {
		super(e);
	}
}
