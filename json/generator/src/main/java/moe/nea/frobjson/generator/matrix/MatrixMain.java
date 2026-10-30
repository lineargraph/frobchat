package moe.nea.frobjson.generator.matrix;

import java.nio.file.Path;

public class MatrixMain {
	public static void main(String[] args) {
		var matrixSpecRoot = Path.of(System.getProperty("matrixApi.matrixSpecRoot", "matrix-spec"));
		var extraSchemasRoot = Path.of(System.getProperty("matrixApi.extraSchemaRoot", "json/matrix/src/main/schemas"));
		var destinationFolder = Path.of(System.getProperty("matrixApi.destinationFolder", "test-output"));
		var matrixGenerator = new MatrixGenerator(matrixSpecRoot, destinationFolder, extraSchemasRoot);
		var clientServerApi = matrixGenerator.apiGenerator("client-server");
		clientServerApi.schemaLibrary().override(ctx -> ctx.ifPath("login.yaml")
			.ifJson(node ->
				node.has("properties")
					&& node.get("properties").has("medium"))
			.thenUseExtraSchema("login.yaml"));
		clientServerApi.schemaLibrary().override(ctx -> ctx.ifPath("definitions/user_identifier.yaml")
			.ifTitle("User identifier")
			.thenUseRelativeClass("definitions.useridentifier.UserIdentifier"));
		clientServerApi.schemaLibrary().override(ctx ->
			ctx.ifPath("room_summary.yaml")
				.ifJson(it -> {
					var node = it.at("/allOf/0/$ref");
					return node.asString("").equals("../client-server/definitions/room_summary.yaml");
				})
				.thenUseExtraSchema("room_summary_response.yaml"));
		clientServerApi.schemaLibrary().override(ctx ->
			ctx.ifPath("room_summary.yaml")
				.ifJson(it -> it.has("$ref") && it.get("$ref").asString().startsWith("../client-server/"))
				.thenUse(() -> new MemberType.FileReference(
					ctx.library(),
					ctx.context.context().folder(),
					ctx.json().get("$ref").asString().substring("../client-server/".length())
				)));

		clientServerApi.generateRoutes("room_summary.yaml");
		clientServerApi.generateRoutes("wellknown.yaml");
		clientServerApi.generateRoutes("whoami.yaml");
		clientServerApi.generateRoutes("admin.yaml");
		clientServerApi.generateRoutes("registration_tokens.yaml");
		clientServerApi.generateRoutes("login.yaml");
		clientServerApi.generateRoutes("keys.yaml");
//		clientServerApi.generateRoutes("sync.yaml");
		matrixGenerator.writeAll();
	}
}
