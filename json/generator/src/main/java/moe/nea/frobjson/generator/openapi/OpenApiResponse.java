package moe.nea.frobjson.generator.openapi;

import moe.nea.frobjson.generator.SchemaType;
import org.jspecify.annotations.Nullable;

public record OpenApiResponse(
	@Nullable String description,
	SchemaType schema
) {
}
