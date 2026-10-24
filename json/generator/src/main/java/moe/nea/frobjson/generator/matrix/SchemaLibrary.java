package moe.nea.frobjson.generator.matrix;

import lombok.SneakyThrows;
import tools.jackson.databind.JsonNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class SchemaLibrary {
	private final MatrixGenerator generator;
	private final String rootPackage;
	private final Path rootPath;
	private final List<Function<MemberType.TypeResolutionContext, MemberType>> memberTypeOverrides = new ArrayList<>();

	public SchemaLibrary(
		MatrixGenerator generator,
		String rootPackage,
		Path rootPath
	) {
		this.generator = generator;
		this.rootPackage = rootPackage;
		this.rootPath = rootPath;
	}

	private Map<Path, Lazy<SchemaReference>> schemaReferences = new HashMap<>();

	@SneakyThrows
	private Lazy<SchemaReference> resolveDefinition0(Path relativePath) {
		return Lazy.of(() -> {
				var fileName = rootPath.resolve(relativePath);
				var rawDefinition = ((MemberType.DirectDeclaration) YAMLUtils.readFile(this, fileName, MemberType.class))
					.type();
				var packageName = packageName(relativePath);
				return new SchemaReference(
					this,
					rawDefinition,
					generator.allocateClassName(packageName, Objects.requireNonNull(rawDefinition.getTitle()))
				);
			}
		);
	}

	public void writeFiles() {
		for (Lazy<SchemaReference> value : schemaReferences.values()) {
			var schemaReference = value.get();
			generator.writeType(schemaReference.generateDefinition());
		}
	}

	@SneakyThrows
	public SchemaReference resolveDefinition(
		Path relativeTo,
		String reference) {
		assert Files.isDirectory(relativeTo) : "Relative root should be the folder containing the file";
		var parts = reference.split("#");
		assert parts.length == 1 : "Nested references not handled yet";
		var relativePath = rootPath.relativize(relativeTo).resolve(parts[0]);
		return schemaReferences.computeIfAbsent(relativePath, this::resolveDefinition0).get();
	}

	private String packageName(Path segments) {
		var packageName = new StringBuilder(rootPackage);
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

	public Path prefix() {
		return generator.dataFolder().relativize(rootPath);
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
			failed = failed || !library().rootPath.relativize(context.context().file())
				.toString().equals(path);
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
				thenUse(library().generator.extraSchemasRoot()
					.resolve(library().prefix())
					.resolve(path));
		}

		public void thenUse(Path path) {
			if (!failed)
				thenUse(new MemberType.TypeOverride(path, library()));
		}

		public void thenUse(MemberType memberType) {
			this.memberType = memberType;
		}

		public MemberType result() {
			if (failed) return null;
			return memberType;
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
		return rootPackage;
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
