package moe.nea.frobjson.generator;

import java.util.HashSet;
import java.util.Set;

public class NameCollection {

	private Set<String> names = new HashSet<>();

	public String foldName(String name) {
		return name.replace(" ", "").replace(".", ""); // TODO: proper name replacement
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
