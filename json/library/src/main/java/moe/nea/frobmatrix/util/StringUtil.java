package moe.nea.frobmatrix.util;

import java.util.function.Supplier;

public class StringUtil {
	public static Iterable<String> split(String text, String delimiter) {
		return IterUtil.createPollOnceIterable(new Supplier<>() {
			int lastIndex = 0;
			int nextIndex = 0;

			@Override
			public String get() {
				if (nextIndex < 0) {
					return null;
				}
				lastIndex = nextIndex + delimiter.length();
				nextIndex = text.indexOf(delimiter, lastIndex);
				if (nextIndex < 0) {
					return text.substring(lastIndex);
				}
				return text.substring(lastIndex, nextIndex);
			}
		});
	}
}
