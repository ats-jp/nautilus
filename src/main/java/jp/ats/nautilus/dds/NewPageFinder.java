package jp.ats.nautilus.dds;

public class NewPageFinder {

	private int currentPosition = 0;

	public boolean newPage(String line) {
		String header = line.substring(0, 5);
		int number = Integer.parseInt(line.substring(0, 5).trim());

		if (header.charAt(3) != ' ') {
			currentPosition += number;
		} else {
			//改ページ
			if (number < currentPosition + 1) {
				currentPosition = 0;
				return true;
			}

			currentPosition = number - 1;
		}

		return false;
	}

	public int getCurrentPosition() {
		return currentPosition;
	}
}
