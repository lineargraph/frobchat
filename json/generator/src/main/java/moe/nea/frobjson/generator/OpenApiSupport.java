package moe.nea.frobjson.generator;

import com.google.gson.JsonElement;
import moe.nea.frobjson.internal.JsonUtil;

import java.util.Objects;
import java.util.stream.Collectors;

public class OpenApiSupport {
	public static void generateAllSchemasFromOpenApi(
		GenerationContext ctx, JsonElement openApiDoc
	) {
		JsonUtil.streamEntriesOrEmpty(openApiDoc.getAsJsonObject().get("paths"))
			.flatMap(path -> JsonUtil.streamEntriesOrEmpty(path.getValue())
				.flatMap(operation -> {
					var operationObj = operation.getValue().getAsJsonObject();
					var operationId = JsonUtil.getStringOrNull(operationObj.get("operationId"));
					return JsonUtil.streamEntriesOrEmpty(operationObj.get("responses"))
						.map(status -> {
							var statusObj = status.getValue().getAsJsonObject();
							var description = JsonUtil.getStringOrNull(statusObj.get("description"));
							var json = JsonUtil.jsonObjectOrEmpty(statusObj.get("content")).get("application/json");
							if (json != null) {
								var statusSuffix = (status.getKey().equals("200") ? "" : status.getKey());
								var name =
									operationId != null
										? operationId
										: path;
								return ctx.getSchemaForProperty(name + statusSuffix, json.getAsJsonObject().get("schema"), null);
							}
							return null;
						})
						.filter(Objects::nonNull);
				}))
			.forEach(it -> System.out.println("Schema collected " + it));
	}
}
