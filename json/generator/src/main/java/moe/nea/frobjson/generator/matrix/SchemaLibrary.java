package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.ClassName;
import lombok.SneakyThrows;
import moe.nea.frobjson.generator.NameCollection;
import moe.nea.frobjson.internal.StreamUtil;
import tools.jackson.databind.JsonNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SchemaLibrary {
	private final MatrixGenerator generator;
	private final List<Function<MemberType.TypeResolutionContext, MemberType>> memberTypeOverrides = new ArrayList<>();

	public MatrixGenerator matrixGenerator() {
		return generator;
	}

	public SchemaLibrary(
		MatrixGenerator generator
	) {
		this.generator = generator;
	}

//	public String basePackage() {
//		return generator.basePackage() + "." + prefix.replace("-", "").replace("_", "").replace("/", ".");
//	}

	//	public Path dataRoot() {
//		return generator.dataFolder().resolve(prefix);
//	}

	public enum DataRoot {
		SPEC,
		EXTRA
	}

	public record SchemaReferencePath(
		Path relativeToDataRoot,
		DataRoot dataRoot
	) {
		public SchemaReferencePath {
			assert !relativeToDataRoot.isAbsolute();
			assert StreamUtil.fromIterable(relativeToDataRoot).noneMatch(it -> it.toString().contains(".."));
		}

		public Path resolve(MatrixGenerator matrixGenerator) {
			return switch (dataRoot) {
				case SPEC -> matrixGenerator.dataFolder().resolve(relativeToDataRoot);
				case EXTRA -> matrixGenerator.extraSchemasRoot().resolve(relativeToDataRoot);
			};
		}
	}

	private final Map<SchemaReferencePath, Lazy<SchemaReference>> schemaReferences = new HashMap<>();

	private SchemaReference coerceMemberTypeToSchemaReference(
		MemberType memberType,
		SchemaReferencePath path
	) {
		switch (memberType) {
			case MemberType.NonReference rawDefinition -> {
				var packageName = packageName(memberType, path);
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
				return coerceMemberTypeToSchemaReference(delegatingType.delegate(), path);
			}
		}
	}

	@SneakyThrows
	private Lazy<SchemaReference> resolveDefinition0(SchemaReferencePath referencePath) {
		return generator.pushLazy(Lazy.of(() -> {
				try {
					return coerceMemberTypeToSchemaReference(YAMLUtils.readFile(this, referencePath, MemberType.class), referencePath);
				} catch (Throwable e) {
					throw new RuntimeException("While reading schema definition from " + referencePath.resolve(matrixGenerator()), e);
				}
			}
		));
	}

	@SneakyThrows
	public SchemaReference resolveDefinition(
		SchemaReferencePath relativeTo,
		String reference) {
		SchemaReferencePath referencePath;
		var parts = reference.split("#");
		assert parts.length == 1 : "Nested references not handled yet";
		if (parts[0].startsWith("SPEC_ROOT/")) {
			referencePath = new SchemaReferencePath(
				Path.of(parts[0].substring("SPEC_ROOT/".length())),
				DataRoot.SPEC
			);
		} else {
			referencePath = new SchemaReferencePath(
				relativeTo.relativeToDataRoot.getParent().resolve(parts[0]),
				relativeTo.dataRoot
			);
		}
		return schemaReferences.computeIfAbsent(referencePath, this::resolveDefinition0).get();
	}

	private String packageName(MemberType memberType, SchemaReferencePath path) {
		var packageName = new StringBuilder(matrixGenerator().basePackage());
		for (Path segment : path.relativeToDataRoot()) {
			var segmentName = segment.toString();
			if (segmentName.endsWith(".yaml"))
				segmentName = segmentName.substring(0, segmentName.length() - 5);
			segmentName = segmentName.replace("_", "");
			segmentName = segmentName.replace("-", "");
			packageName.append(".").append(segmentName);
		}
		return packageName.toString();
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

		public TypeResolutionContextHelper ifPath(Predicate<String> predicate) {
			return ifReferencePath(path -> predicate.test(path.relativeToDataRoot.toString()));
		}

		public TypeResolutionContextHelper ifReferencePath(Predicate<SchemaReferencePath> predicate) {
			failed = failed || !predicate.test(context.context().file());
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
				thenUse(new SchemaReferencePath(Path.of(path), DataRoot.EXTRA));
		}

		public void thenUse(SchemaReferencePath path) {
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
	}

	public void override(Consumer<TypeResolutionContextHelper> condition) {
		memberTypeOverrides.add(context -> {
			var helper = new TypeResolutionContextHelper(context);
			condition.accept(helper);
			return helper.result();
		});
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
