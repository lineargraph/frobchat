package moe.nea.frobjson.generator;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

public record SchemaProperty(
	String propName,
	String fieldName,
	SchemaType type,
	boolean required
) {
	TypeName fieldType() {
		var fieldType = type.typeName();
		if (!required) {
			fieldType = fieldType.annotated(AnnotationSpec.builder(Nullable.class).build());
		}
		return fieldType;
	}
}
