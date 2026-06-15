package moe.nea.frobjson.generator.openapi;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.TypeName;
import moe.nea.frobjson.generator.SchemaType;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public record OpenApiParameter(
	String name,
	String fieldName,
	String description,
	In in,
	boolean required,
	SchemaType schema
) {
	public TypeName type() {
		var typ = schema.typeName();
		if (!required)
			typ = typ.annotated(AnnotationSpec.builder(Nullable.class).build());
		return typ;
	}

	public enum In {
		QUERY,
		PATH,
		HEADER
	}
}
