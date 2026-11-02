package moe.nea.frobjson.generator.matrix;

import lombok.extern.slf4j.Slf4j;
import moe.nea.frobjson.generator.NameCollection;

import java.nio.file.Path;
import java.util.Locale;

@Slf4j
public class ApiGenerator {
	private final MatrixGenerator matrixGenerator;
	private final String prefix;

	private final RouteCategoryGenerator routeCategoryGenerator = new RouteCategoryGenerator();
	private final String typeLC;

	public MatrixGenerator matrixGenerator() {
		return matrixGenerator;
	}

	public ApiGenerator(MatrixGenerator matrixGenerator, String type) {
		this.matrixGenerator = matrixGenerator;
		this.typeLC = type.toLowerCase(Locale.ROOT);
		prefix = "api/" + type;
		matrixGenerator.getPackageNameSpace(apiBasePackage())
			.createChild(NameCollection.foldName(type + " api", NameCollection.NameType.CLASS_NAME))
			.setGenerator(routeCategoryGenerator);
	}

	public String apiBasePackage() {
		return matrixGenerator.basePackage() + "."
			+ prefix.replace("_", "").replace("-", "").replace("/", ".");
	}

	public void generateRoutes(String path) {
		var rawRoutes = YAMLUtils.readFile(
			matrixGenerator.schemaLibrary(),
			new SchemaLibrary.SchemaReferencePath(Path.of(prefix).resolve(path), SchemaLibrary.DataRoot.SPEC),
			RawOpenApiRoutes.class);
		var title = rawRoutes.getInfo().getTitle()
			.toLowerCase()
			.replace("matrix", "")
			.replace(typeLC, "");
		var packageName = apiBasePackage() + ".routes." + path.replace("-", "").replace("_", "").replace(".yaml", "");
		var className = NameCollection.foldName(title/* + " Routes"*/, NameCollection.NameType.CLASS_NAME);
		var cns = matrixGenerator.getPackageNameSpace(packageName)
			.createChild(className);
		routeCategoryGenerator.routes.add(cns);
		cns.setGenerator(new RoutesGenerator(rawRoutes, packageName));
	}
}
