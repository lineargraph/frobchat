package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.ClassName;
import lombok.SneakyThrows;
import moe.nea.frobjson.generator.NameCollection;
import tools.jackson.databind.JsonNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SchemaLibrary {
	private final MatrixGenerator generator;
	private final String prefix;
	private final List<Function<MemberType.TypeResolutionContext, MemberType>> memberTypeOverrides = new ArrayList<>();

	public MatrixGenerator matrixGenerator() {
		return generator;
	}

	public SchemaLibrary(
		MatrixGenerator generator,
		String prefix
	) {
		this.generator = generator;
		this.prefix = prefix;
	}

	public String basePackage() {
		return generator.basePackage() + "." + prefix.replace("-", "").replace("_", "").replace("/", ".");
	}

	public Path dataRoot() {
		return generator.dataFolder().resolve(prefix);
	}

	private final Map<Path, Lazy<SchemaReference>> schemaReferences = new HashMap<>();

	private SchemaReference coerceMemberTypeToSchemaReference(
		MemberType memberType,
		Path relativePath
	) {
		switch (memberType) {
			case MemberType.NonReference rawDefinition -> {
				var packageName = packageName(relativePath);
				var cns = generator.getPackageNameSpace(packageName)
					.createChild(NameCollection.foldName(Objects.requireNonNull(rawDefinition.guessTitle(), () -> "cannot find name of " + rawDefinition), NameCollection.NameType.CLASS_NAME));
				switch (rawDefinition) {
					case MemberType.DirectDeclaration directDeclaration ->
						cns.setGenerator(new ObjectTypeGenerator((RawJsonType.ObjectType) directDeclaration.type()));
					case MemberType.ExtendsDefinition extendsDefinition ->
						cns.setGenerator(new ExtendsTypeGenerator(extendsDefinition.getParent(), extendsDefinition.getOverrides()));
				}
				return new SchemaReference(cns);
			}
			case MemberType.ClassReference reference -> {
				return reference.schemaReference();
			}
			case MemberType.FileReference reference ->
				throw new RuntimeException("Recursive references shall not be tolerated");
			case MemberType.EitherType eitherType ->
				throw new RuntimeException("Either type definition shall not be tolerated");
			case MemberType.DelegatingType delegatingType -> {
				return coerceMemberTypeToSchemaReference(delegatingType.delegate(), relativePath);
			}
		}
	}

	@SneakyThrows
	private Lazy<SchemaReference> resolveDefinition0(Path relativePath) {
		return generator.pushLazy(Lazy.of(() -> {
				try {
					var fileName = dataRoot().resolve(relativePath);
					return coerceMemberTypeToSchemaReference(YAMLUtils.readFile(this, fileName, MemberType.class), relativePath);
				} catch (Exception e) {
					throw new RuntimeException("While reading schema definition from " + relativePath, e);
				}
			}
		));
	}

	@SneakyThrows
	public SchemaReference resolveDefinition(
		Path relativeTo,
		String reference) {
		assert Files.isDirectory(relativeTo) : "Relative root should be the folder containing the file";
		if (reference.startsWith("LIBRARY_ROOT/")) {
			relativeTo = dataRoot();
			reference = reference.substring("LIBRARY_ROOT/".length());
		}
		var parts = reference.split("#");
		assert parts.length == 1 : "Nested references not handled yet";
		var relativePath = dataRoot().relativize(relativeTo).resolve(parts[0]);
		return schemaReferences.computeIfAbsent(relativePath, this::resolveDefinition0).get();
	}

	private String packageName(Path segments) {
		var packageName = new StringBuilder(basePackage());
		for (Path segment : segments) {
			var segmentName = segment.toString();
			if (segmentName.endsWith(".yaml"))
				segmentName = segmentName.substring(0, segmentName.length() - 5);
			segmentName = segmentName.replace("_", "");
			segmentName = segmentName.replace("-", "");
			packageName.append(".").append(segmentName);
		}
		return packageName.toString();
	}

	public String prefix() {
		return prefix;
	}

	public Path extraRoot() {
		return generator.extraSchemasRoot().resolve(prefix);
	}

	public static class TypeResolutionContextHelper {
		public final MemberType.TypeResolutionContext context;
		public boolean failed;
		public MemberType memberType;

		public TypeResolutionContextHelper(MemberType.TypeResolutionContext context) {
			this.context = context;
		}

		public SchemaLibrary library() {
			return context.context().library();
		}

		public JsonNode json() {
			return context.node();
		}

		public TypeResolutionContextHelper ifJson(Predicate<JsonNode> nodeTest) {
			failed |= !nodeTest.test(json());
			return this;
		}

		public TypeResolutionContextHelper ifPath(String path) {
			return ifPath(path::equals);
		}

		public TypeResolutionContextHelper ifPath(Predicate<String> path) {
			failed = failed || !path.test(library().dataRoot().relativize(context.context().file()).toString());
			return this;
		}

		public TypeResolutionContextHelper ifTitle(String title) {
			failed = failed || !title.equals(context.title());
			return this;
		}

		public boolean failed() {
			return failed;
		}

		public void thenUseExtraSchema(String path) {
			if (!failed)
				thenUse(library().extraRoot().resolve(path));
		}

		public void thenUse(Path path) {
			if (!failed)
				thenUse(new MemberType.TypeOverride(path, library()));
		}

		public void thenUse(MemberType memberType) {
			this.memberType = memberType;
		}

		public void thenUse(Supplier<MemberType> memberType) {
			if (!failed)
				this.memberType = memberType.get();
		}

		public MemberType result() {
			if (failed) return null;
			return memberType;
		}

		public void thenUseClass(String className) {
			if (!failed)
				thenUse(new MemberType.ClassReference(library(), ClassName.bestGuess(className)));
		}

		public void thenUseRelativeClass(String relativeClassName) {
			if (!failed)
				thenUseClass(library().basePackage() + "." + relativeClassName);
		}
	}

	public void override(Consumer<TypeResolutionContextHelper> condition) {
		memberTypeOverrides.add(context -> {
			var helper = new TypeResolutionContextHelper(context);
			condition.accept(helper);
			return helper.result();
		});
	}

	public String rootPackage() {
		return basePackage();
	}

	public MemberType resolveOverride(MemberType.TypeResolutionContext typeResolutionContext) {
		for (var override : memberTypeOverrides) {
			var result = override.apply(typeResolutionContext);
			if (result != null)
				return result;
		}
		return null;
	}
}
