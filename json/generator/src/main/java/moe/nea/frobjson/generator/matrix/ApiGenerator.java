package moe.nea.frobjson.generator.matrix;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
public class ApiGenerator {
	private final MatrixGenerator matrixGenerator;
	private final String type;
	private final SchemaLibrary schemaLibrary;
	private final Path rootFolder;

	public SchemaLibrary schemaLibrary() {
		return schemaLibrary;
	}

	public Path rootFolder() {
		return rootFolder;
	}

	public ApiGenerator(MatrixGenerator matrixGenerator, String type) {
		this.matrixGenerator = matrixGenerator;
		this.type = type;
		this.rootFolder = matrixGenerator.dataFolder().resolve("api").resolve(type);
		this.schemaLibrary = new SchemaLibrary(matrixGenerator,
			matrixGenerator.basePackage() + ".api." + type.replace("-", ""),
			rootFolder
		);
	}

	@SneakyThrows
	public RawOpenApiRoutes generateRoutes(String path) {
		return YAMLUtils.readFile(schemaLibrary, rootFolder.resolve(path), RawOpenApiRoutes.class);
	}
}
