package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.ClassName;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatrixGenerator {
	private final Path matrixSpecRoot;
	private final Path destinationFolder;
	private final Path extraSchemasRoot;
	private final String basePackage = "moe.nea.frobmatrix";

	private List<Runnable> queuedTasks = new ArrayList<>();

	public void complete() {
		while (!queuedTasks.isEmpty()) {
			var ptr = queuedTasks;
			queuedTasks = new ArrayList<>();
			for (var p : ptr) {
				p.run();
			}
		}
	}

	public void queueTask(Runnable runnable) {
		queuedTasks.add(runnable);
	}

	public Path extraSchemasRoot() {
		return extraSchemasRoot;
	}

	public String basePackage() {
		return basePackage;
	}

	private Map<String, NameSpace.PackageNameSpace> nameSpaceMap = new HashMap<>();

	public NameSpace.PackageNameSpace getPackageNameSpace(String packageName) {
		return nameSpaceMap.computeIfAbsent(packageName, it -> new NameSpace.PackageNameSpace(this, it));
	}

	public NameSpace.ClassNameSpace getClassNameSpace(ClassName className) {
		NameSpace ns = getPackageNameSpace(className.packageName());
		for (String s : className.simpleNames()) {
			ns = ns.getExistingChild(s);
		}
		assert ns instanceof NameSpace.ClassNameSpace;
		return (NameSpace.ClassNameSpace) ns;
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

	public MatrixGenerator(Path matrixSpecRoot, Path destinationFolder, Path extraSchemasRoot) {
		this.matrixSpecRoot = matrixSpecRoot;
		this.destinationFolder = destinationFolder;
		this.extraSchemasRoot = extraSchemasRoot;
	}

	public <T> Lazy<T> pushLazy(Lazy<T> lazy) {
		queueTask(lazy::get);
		return lazy;
	}

	public void writeAll() {
		complete();
		for (var pns : nameSpaceMap.values()) {
			pns.writeTo(this.destinationFolder());
		}
	}
}
