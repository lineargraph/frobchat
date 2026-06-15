package moe.nea.frobjson.generator;

import com.google.gson.JsonElement;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.TypeName;
import moe.nea.frobjson.internal.JsonUtil;
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

	CodeBlock deserializerExpression(String jsonObjectExpression) {
		var deserializer = type().deserializeLambda("$jsonField");
		if (!required()) {
			deserializer = CodeBlock.of("$T.<$T, $T>liftNullable($L)", JsonUtil.class, JsonElement.class, fieldType().withoutAnnotations(), deserializer);
		}
		return CodeBlock.of(
			"$T.bind($L.get($S), $L)",
			JsonUtil.class,
			jsonObjectExpression,
			propName(),
			deserializer
		);
	}

	CodeBlock serializerExpression(String objectExpression) {
		var serializer = type().serializeLambda("$field");
		if (!required()) {
			serializer = CodeBlock.of("$T.<$T, $T>liftNullable($L)", JsonUtil.class, fieldType().withoutAnnotations(), JsonElement.class, serializer);
		}
		return CodeBlock.of(
			"$T.bind($L.$L, $L)",
			JsonUtil.class,
			objectExpression,
			fieldName(),
			serializer
		);
	}
}
