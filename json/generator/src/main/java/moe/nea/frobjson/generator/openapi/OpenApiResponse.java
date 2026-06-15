package moe.nea.frobjson.generator.openapi;

import moe.nea.frobjson.generator.SchemaType;

public record OpenApiResponse(
	String description,
	SchemaType schema
) {
}
