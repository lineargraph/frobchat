package moe.nea.frobjson.generator;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import moe.nea.frobjson.generator.openapi.OpenApiSupport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class GenerateOpenApiSchemas {
	public static void main(
		String[] args
	) throws IOException {
		var sourceFile = Path.of(args[0]);
		var packageName = args[1];
		var destinationDirectory = Path.of(args[2]);
		var ctx = new GenerationContext(packageName + ".models");
		ctx.operationPackageName = packageName + ".operations";

		JsonElement openApiJson;
		try (var input = Files.newBufferedReader(sourceFile, StandardCharsets.UTF_8)) {
			openApiJson = new Gson().fromJson(input, JsonElement.class);
		}

		OpenApiSupport.generateAllSchemasFromOpenApi(ctx, openApiJson)
			.forEach(it -> {
				try {
					it.writeTo(destinationDirectory);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});

		ctx.writeClosure(destinationDirectory);
	}
}
