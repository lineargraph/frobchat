package moe.nea.frobjson.generator.matrix;

import java.nio.file.Path;

public class MatrixMain {
	public static void main(String[] args) {
		var matrixSpecRoot = Path.of(System.getProperty("matrixApi.matrixSpecRoot", "matrix-spec"));
		var destinationFolder = Path.of(System.getProperty("matrixApi.destinationFolder", "test-output"));
		var matrixGenerator = new MatrixGenerator(matrixSpecRoot, destinationFolder);
		var clientServerApi = matrixGenerator.apiGenerator("client-server");
		var schema = clientServerApi.schemaLibrary().resolveDefinition(
			clientServerApi.rootFolder(),
			"definitions/errors/error.yaml");
		clientServerApi.schemaLibrary().resolveDefinition(
			clientServerApi.rootFolder().resolve("definitions"),
			"errors/error.yaml");
		System.out.println(schema.getClassName());
		System.out.println(schema.getRawJsonType());
		var wellKnownRoutes = clientServerApi.generateRoutes("wellknown.yaml");
		System.out.println(wellKnownRoutes);
		clientServerApi.schemaLibrary().writeFiles();
	}
}
