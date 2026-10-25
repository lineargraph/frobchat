package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public sealed abstract class NameSpace permits NameSpace.PackageNameSpace, NameSpace.ClassNameSpace {
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

	public static final class PackageNameSpace extends NameSpace {
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
	}

	public static final class ClassNameSpace extends NameSpace {
		@Getter
		private final NameSpace parent;
		@Getter
		private final ClassName className;
		@Getter
		private ClassGenerator generator;

		private boolean emitted = false;

		private static boolean IN_GENERATION_PHASE = false;

		public void setGenerator(ClassGenerator generator) {
			assert this.generator == null;
			if (IN_GENERATION_PHASE)
				throw new RuntimeException("Cannot set more generators while generating");
			getMatrixGenerator().queueTask(this::prepareGeneration);
			this.generator = generator;
		}

		private void prepareGeneration() {
			if (emitted) return;
			emitted = true;
			try {
				generator.emitDependencies(this);
			} catch (Exception e) {
				throw new RuntimeException("While preparing generation of " + getClassName(), e);
			}
		}

		public PackageNameSpace getPackageNameSpace() {
			return switch (getParent()) {
				case PackageNameSpace pns -> pns;
				case ClassNameSpace cns -> cns.getPackageNameSpace();
			};
		}

		public ClassNameSpace(NameSpace parent, String className) {
			super(parent.getMatrixGenerator());
			this.parent = parent;
			this.className = switch (parent) {
				case PackageNameSpace pns -> ClassName.get(pns.getPackageName(), className);
				case ClassNameSpace cns -> cns.getClassName().nestedClass(className);
			};
			parent.register(this);
		}

		private boolean isTopLevelClass() {
			return className.topLevelClassName() == className;
		}

		public TypeSpec buildClassRecursively() {
			try {
				assert emitted;
				var builder = TypeSpec.classBuilder(className);
				if (!isTopLevelClass())
					builder.addModifiers(Modifier.STATIC);
				generator.writeType(builder, this);

				for (var nestedCns : getChildren().values()) {
					builder.addType(nestedCns.buildClassRecursively());
				}
				return builder.build();
			} catch (Exception e) {
				throw new RuntimeException("While generating TypeSpec for " + getClassName(), e);
			}
		}

		@SneakyThrows
		public void writeTo(Path sourceRoot) {
			IN_GENERATION_PHASE = true;
			assert emitted;
			assert isTopLevelClass();
			JavaFile.builder(className.packageName(), buildClassRecursively())
				.indent("\t")
				.build()
				.writeTo(sourceRoot);
		}
	}
}
