package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;

public final class ClassNameSpace extends NameSpace {
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
		} catch (Throwable e) {
			throw new RuntimeException("While preparing generation of " + getClassName(), e);
		}
	}

	public PackageNameSpace getPackageNameSpace() {
		return switch (getParent()) {
			case PackageNameSpace pns -> pns;
			case moe.nea.frobjson.generator.matrix.ClassNameSpace cns -> cns.getPackageNameSpace();
		};
	}

	public ClassNameSpace(NameSpace parent, String className) {
		super(parent.getMatrixGenerator());
		this.parent = parent;
		this.className = switch (parent) {
			case PackageNameSpace pns -> ClassName.get(pns.getPackageName(), className);
			case moe.nea.frobjson.generator.matrix.ClassNameSpace cns -> cns.getClassName().nestedClass(className);
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
		} catch (Throwable e) {
			throw new RuntimeException("While generating TypeSpec for " + getClassName(), e);
		}
	}

	@SneakyThrows
	public void writeTo(Path sourceRoot) {
		IN_GENERATION_PHASE = true;
		assert emitted;
		assert isTopLevelClass();
		if (generator instanceof PreExistingGenerator) return;
		JavaFile.builder(className.packageName(), buildClassRecursively())
			.indent("\t")
			.build()
			.writeTo(sourceRoot);
	}

	@Override
	public String toString() {
		return "ClassNameSpace(" + className + ")";
	}
}
