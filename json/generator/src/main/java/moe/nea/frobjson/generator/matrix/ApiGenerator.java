package moe.nea.frobjson.generator.matrix;

import lombok.extern.slf4j.Slf4j;
import moe.nea.frobjson.generator.NameCollection;

import java.nio.file.Path;

@Slf4j
public class ApiGenerator {
	private final MatrixGenerator matrixGenerator;
	private final SchemaLibrary schemaLibrary;

	public MatrixGenerator matrixGenerator() {
		return matrixGenerator;
	}

	public SchemaLibrary schemaLibrary() {
		return schemaLibrary;
	}

	public ApiGenerator(MatrixGenerator matrixGenerator, String type) {
		this.matrixGenerator = matrixGenerator;
		var prefix = "api/" + type;
		this.schemaLibrary = new SchemaLibrary(
			matrixGenerator,
			prefix
		);
	}

	public void generateRoutes(String path) {
		var rawRoutes = YAMLUtils.readFile(schemaLibrary, schemaLibrary.dataRoot().resolve(path), RawOpenApiRoutes.class);
		var title = rawRoutes.getInfo().getTitle();
		var packageName = schemaLibrary.rootPackage() + ".routes." + path.replace("-", "").replace("_", "").replace(".yaml", "");
		var className = NameCollection.foldName(title/* + " Routes"*/, NameCollection.NameType.CLASS_NAME);
		matrixGenerator.getPackageNameSpace(packageName)
			.createChild(className)
			.setGenerator(new RoutesGenerator(this, rawRoutes, packageName));
	}
}
