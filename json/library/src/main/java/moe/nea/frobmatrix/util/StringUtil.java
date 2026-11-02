package moe.nea.frobmatrix.util;

import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StringUtil {
	public static String getInitials(String name, int maxInitials) {
		var stream = StreamUtil.fromIterable(StringUtil.split(name, " "))
			.map(String::trim)
			.filter(str -> !str.isEmpty())
			.map(str -> str.substring(0, 1));
		if (maxInitials > 0)
			stream = stream.limit(maxInitials);
		return stream.collect(Collectors.joining(""));
	}

	public static Iterable<String> split(String text, String delimiter) {
		return IterUtil.createPollOnceIterable(new Supplier<>() {
			int lastMatchEnd = 0;

			@Override
			public String get() {
				if (lastMatchEnd > text.length())
					return null;
				var nextMatchStart = text.indexOf(delimiter, lastMatchEnd);
				if (nextMatchStart < 0) {
					var ret = text.substring(lastMatchEnd);
					lastMatchEnd = text.length() + 1;
					return ret;
				}
				var ret = text.substring(lastMatchEnd, nextMatchStart);
				lastMatchEnd = nextMatchStart + delimiter.length();
				return ret;
			}
		});
	}
}
