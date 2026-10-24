package moe.nea.frobjson.generator.matrix;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
public class ApiGenerator {
	private final MatrixGenerator matrixGenerator;
	private final String type;
	private final SchemaLibrary schemaLibrary;
	private final Path rootFolder;

	public MatrixGenerator matrixGenerator() {
		return matrixGenerator;
	}

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

	public void generateRoutes(String path) {
		var rawRoutes = YAMLUtils.readFile(schemaLibrary, rootFolder.resolve(path), RawOpenApiRoutes.class);
		var packageName = schemaLibrary.rootPackage() + ".routes." + path.replace("-", "").replace(".yaml", "");
		for (var file : new RoutesGenerator(this, rawRoutes, packageName).generate()) {
			matrixGenerator.writeType(file);
		}
	}
}
