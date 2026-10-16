package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.ClassName;
import moe.nea.frobjson.generator.NameCollection;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MatrixGenerator {
	private final Path matrixSpecRoot;
	private final Path destinationFolder;
	private final String basePackage = "moe.nea.frobmatrix";

	public String basePackage() {
		return basePackage;
	}

	private Map<String, NameCollection> classNames = new HashMap<>();

	public NameCollection classNamesForPackage(String packageName) {
		return classNames.computeIfAbsent(packageName, key -> new NameCollection(NameCollection.NameType.CLASS_NAME, true));
	}

	public Path matrixSpecRoot() {
		return matrixSpecRoot;
	}

	public Path dataFolder() {
		return matrixSpecRoot.resolve("data");
	}

	private final Map<String, ApiGenerator> apiGenerators = new HashMap<>();

	public ApiGenerator apiGenerator(String type) {
		return apiGenerators.computeIfAbsent(type, key -> new ApiGenerator(this, type));
	}

	public Path destinationFolder() {
		return destinationFolder;
	}

	public MatrixGenerator(Path matrixSpecRoot, Path destinationFolder) {
		this.matrixSpecRoot = matrixSpecRoot;
		this.destinationFolder = destinationFolder;
	}

	public ClassName allocateClassName(String packageName, String title) {
		return ClassName.get(packageName, classNamesForPackage(packageName).allocateName(title));
	}
}
