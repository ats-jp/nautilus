package jp.ats.nautilus.dds;

import java.util.LinkedList;
import java.util.List;

import jp.ats.nautilus.common.U;

/**
 * フィールドが重複していない重複行を結合します。
 * フィールドが重複している場合は、元のままです。
 */
public class ZeroLineConcatenator {

	private static final byte space = " ".getBytes(AS400Utilities.CHARSET)[0];

	public static byte[][] execute(byte[][] as400Data) {
		byte[] headerBuffer = new byte[4];

		List<byte[]> result = U.newLinkedList();

		LinkedList<byte[]> concatTargets = U.newLinkedList();

		for (byte[] line : as400Data) {
			System.arraycopy(line, 0, headerBuffer, 0, headerBuffer.length);
			String header = new String(headerBuffer, AS400Utilities.CHARSET);

			if (header.charAt(3) != '0' && concatTargets.size() > 0) {
				concat(concatTargets);
				result.addAll(concatTargets);
				concatTargets.clear();
			}

			concatTargets.add(line);
		}

		concat(concatTargets);
		result.addAll(concatTargets);

		return result.toArray(new byte[result.size()][]);
	}

	private static void concat(LinkedList<byte[]> concatTargets) {
		byte[] base = concatTargets.pop();

		while (concatTargets.size() > 0) {
			byte[] next = concatTargets.pop();

			//重複チェック前の状態を保持
			byte[] undo = base.clone();

			//重複があった場合、結合できたところまでを返す
			if (!concat(base, next)) {
				concatTargets.addFirst(next);
				concatTargets.addFirst(undo);
				return;
			}
		}

		concatTargets.clear();
		concatTargets.add(base);
	}

	private static boolean concat(byte[] base, byte[] next) {
		for (int i = 5; i < base.length; i++) {
			if (base[i] != space && next[i] != space) return false;

			if (base[i] == space) base[i] = next[i];
		}

		return true;
	}
}
