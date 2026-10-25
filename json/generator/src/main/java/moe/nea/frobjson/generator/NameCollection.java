package moe.nea.frobjson.generator;

import com.google.common.base.Splitter;

import javax.naming.Name;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class NameCollection {
	public final NameType nameType;
	public final boolean strict;
	private final Set<String> names = new HashSet<>();

	public enum NameType {
		CLASS_NAME,
		METHOD_NAME,
		FIELD_NAME,
	}

	public NameCollection(boolean classNames) {
		this(classNames ? NameType.CLASS_NAME : NameType.METHOD_NAME, false);
	}

	public NameCollection(NameType nameType, boolean strict) {
		this.nameType = nameType;
		this.strict = strict;
	}

	final static Splitter splitter = Splitter.on(Pattern.compile("[\\-._ :]+|(?<=[a-z0-9])(?=[A-Z])"));

	public static void capitalize(StringBuilder appendable, String name) {
		if (name.isEmpty()) return;
		appendable.append(String.valueOf(name.charAt(0)).toUpperCase(Locale.ROOT))
			.append(name.substring(1).toLowerCase(Locale.ROOT));
	}


	public static String foldName(String name, NameType nameType) {
		StringBuilder b = new StringBuilder();
		for (String s : splitter.split(name)) {
			if (s.isEmpty()) continue;
			if (!b.isEmpty() || nameType == NameType.CLASS_NAME)
				capitalize(b, s);
			else
				b.append(s.toLowerCase(Locale.ROOT));
		}
		if ("default".contentEquals(b))
			return "default_";
		return b.toString();
	}

	public String allocateName(String name) {
		name = foldName(name, nameType);
		for (int j = 1; ; j++) {
			var candidate = j <= 1 ? name : name + j;
			if (names.contains(candidate)) {
				if (strict)
					throw new RuntimeException("Encountered duplicate name " + name);
				continue;
			}
			names.add(candidate);
			return candidate;
		}
	}

	public boolean contains(String candidate) {
		return names.contains(candidate);
	}
}
