package moe.nea.frobjson.generator;

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

	CodeBlock serializerExpression(String jsonObjectExpression) {
		var serializer = type().deserializeLambda("$jsonField");
		if (!required()) {
			serializer = CodeBlock.of("$T.liftNullable($L)", JsonUtil.class, serializer);
		}
		return CodeBlock.of(
			"$T.bind($L.get($S), $L)",
			JsonUtil.class,
			jsonObjectExpression,
			propName(),
			serializer
		);
	}
}
