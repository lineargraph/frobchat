package moe.nea.frobjson.generator.matrix;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public sealed abstract class NameSpace permits PackageNameSpace, ClassNameSpace {
	@Getter
	private final MatrixGenerator matrixGenerator;
	@Getter
	private final Map<String, ClassNameSpace> children = new HashMap<>();

	protected NameSpace(MatrixGenerator matrixGenerator) {
		this.matrixGenerator = matrixGenerator;
	}

	protected void register(ClassNameSpace child) {
		assert child.getParent() == this;
		var childName = child.getClassName().simpleName();
		assert !children.containsKey(childName);
		children.put(childName, child);
	}

	public ClassNameSpace createChild(String name) {
		return new ClassNameSpace(this, name);
	}

	public ClassNameSpace getExistingChild(String name) {
		return children.get(name);
	}

}
