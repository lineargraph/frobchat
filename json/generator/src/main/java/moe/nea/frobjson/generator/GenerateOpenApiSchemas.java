package moe.nea.frobjson.generator;

import com.palantir.javapoet.JavaFile;
import moe.nea.frobjson.generator.openapi.OpenApiSupport;
import moe.nea.frobjson.internal.JsonUtil;
import moe.nea.frobjson.internal.StreamUtil;

import java.io.IOException;
import java.nio.file.Path;

public class GenerateOpenApiSchemas {
	public static void main(
		String[] args
	) throws IOException {
		var destinationDirectory = Path.of(args[0]);
		int i = 0;
		var ctx = new GenerationContext();
		while (++i < args.length) {
			switch (args[i]) {
				case "-operationPackage" -> ctx.operationPackageName = args[++i];
				case "-modelPackage" -> ctx.modelPackageName = args[++i];
				case "-extraTagged" -> TaggedTypes.generate(ctx, JsonUtil.loadJson(Path.of(args[++i])));
				case "-extendType" -> ExtendedType.generate(ctx, JsonUtil.loadJson(Path.of(args[++i])));
				case "-openApi" -> OpenApiSupport.generateAllSchemasFromOpenApi(ctx, JsonUtil.loadJson(Path.of(args[++i])));
				default -> throw new RuntimeException("Unknown argument: " + args[i]);
			}
		}

		ctx.writeClosure(destinationDirectory);
	}
}
