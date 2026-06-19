package moe.nea.frobjson.generator;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class GenerateSingleSchema {
	public static void main(
		String[] args
	) throws IOException {
		var sourceFile = Path.of(args[0]);
		var packageName = args[1];
		var baseName = args[2];
		var destinationDirectory = Path.of(args[3]);
		var ctx = new GenerationContext(packageName);

		JsonElement schemaJson;
		try (var input = Files.newBufferedReader(sourceFile, StandardCharsets.UTF_8)) {
			schemaJson = new Gson().fromJson(input, JsonElement.class);
		}
		ctx.getSchemaForProperty(baseName, schemaJson, null);
		ctx.writeClosure(destinationDirectory);
	}
}
