package moe.nea.frobjson.generator.matrix;

import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SchemaLibrary {
	private final MatrixGenerator generator;
	private final String rootPackage;
	private final Path rootPath;

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
}
