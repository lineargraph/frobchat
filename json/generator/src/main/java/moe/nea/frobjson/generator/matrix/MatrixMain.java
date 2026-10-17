package moe.nea.frobjson.generator.matrix;

import java.nio.file.Path;

public class MatrixMain {
	public static void main(String[] args) {
		var matrixSpecRoot = Path.of(System.getProperty("matrixApi.matrixSpecRoot", "matrix-spec"));
		var destinationFolder = Path.of(System.getProperty("matrixApi.destinationFolder", "test-output"));
		var matrixGenerator = new MatrixGenerator(matrixSpecRoot, destinationFolder);
		var clientServerApi = matrixGenerator.apiGenerator("client-server");
		clientServerApi.generateRoutes("wellknown.yaml");
		clientServerApi.generateRoutes("whoami.yaml");
		clientServerApi.generateRoutes("admin.yaml");
		clientServerApi.schemaLibrary().writeFiles();
	}
}
