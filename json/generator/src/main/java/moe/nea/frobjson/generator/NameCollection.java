package moe.nea.frobjson.generator;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class NameCollection {
	public final boolean classNames;
	private final Set<String> names = new HashSet<>();

	public NameCollection(boolean classNames) {
		this.classNames = classNames;
	}

	final static Splitter splitter = Splitter.on(Pattern.compile("[\\-._ ]+|(?<=[a-z0-9])(?=[A-Z])"));

	public static void capitalize(StringBuilder appendable, String name) {
		if (name.isEmpty()) return;
		appendable.append(String.valueOf(name.charAt(0)).toUpperCase(Locale.ROOT))
			.append(name.substring(1).toLowerCase(Locale.ROOT));
	}


	public String foldName(String name) {
		StringBuilder b = new StringBuilder();
		for (String s : splitter.split(name)) {
			if (!b.isEmpty() || classNames)
				capitalize(b, s);
			else
				b.append(s.toLowerCase(Locale.ROOT));
		}
		return b.toString();
	}

	public String allocateName(String name) {
		name = foldName(name);
		for (int j = 1; ; j++) {
			var candidate = j <= 1 ? name : name + j;
			if (names.contains(candidate)) continue;
			names.add(candidate);
			return candidate;
		}
	}

	public boolean contains(String candidate) {
		return names.contains(candidate);
	}
}
