package moe.nea.frobjson.generator.openapi;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.TypeName;
import moe.nea.frobjson.generator.SchemaType;
import org.jspecify.annotations.Nullable;

public record OpenApiRequestBody(
	boolean required,
	SchemaType schema
) {
	public TypeName fieldType() {
		var typ = schema.typeName();
		if (!required)
			typ = typ.annotated(AnnotationSpec.builder(Nullable.class).build());
		return typ;

	}
}
