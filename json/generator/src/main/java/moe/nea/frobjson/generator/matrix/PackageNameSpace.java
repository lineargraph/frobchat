package moe.nea.frobjson.generator.matrix;

import lombok.Getter;

import java.nio.file.Path;

public final class PackageNameSpace extends NameSpace {
	@Getter
	private final String packageName;

	public PackageNameSpace(MatrixGenerator matrixGenerator, String packageName) {
		super(matrixGenerator);
		this.packageName = packageName;
	}

	public void writeTo(Path sourceRoot) {
		for (var child : getChildren().values()) {
			child.writeTo(sourceRoot);
		}
	}

	@Override
	public String toString() {
		return "PackageNameSpace(" + packageName + ")";
	}
}
