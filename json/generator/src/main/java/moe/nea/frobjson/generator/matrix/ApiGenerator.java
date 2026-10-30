package moe.nea.frobjson.generator.matrix;

import lombok.extern.slf4j.Slf4j;
import moe.nea.frobjson.generator.NameCollection;

import java.nio.file.Path;

@Slf4j
public class ApiGenerator {
	private final MatrixGenerator matrixGenerator;
	private final String prefix;

	public MatrixGenerator matrixGenerator() {
		return matrixGenerator;
	}

	public ApiGenerator(MatrixGenerator matrixGenerator, String type) {
		this.matrixGenerator = matrixGenerator;
		prefix = "api/" + type;
	}

	public void generateRoutes(String path) {
		var rawRoutes = YAMLUtils.readFile(
			matrixGenerator.schemaLibrary(),
			new SchemaLibrary.SchemaReferencePath(Path.of(prefix).resolve(path), SchemaLibrary.DataRoot.SPEC),
			RawOpenApiRoutes.class);
		var title = rawRoutes.getInfo().getTitle();
		var packageName = matrixGenerator.basePackage() + "."
			+ prefix.replace("_", "").replace("-", "").replace("/", ".")
			+ ".routes." + path.replace("-", "").replace("_", "").replace(".yaml", "");
		var className = NameCollection.foldName(title/* + " Routes"*/, NameCollection.NameType.CLASS_NAME);
		matrixGenerator.getPackageNameSpace(packageName)
			.createChild(className)
			.setGenerator(new RoutesGenerator(rawRoutes, packageName));
	}
}
