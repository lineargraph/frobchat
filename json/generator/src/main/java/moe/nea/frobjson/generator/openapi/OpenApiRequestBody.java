package moe.nea.frobjson.generator.openapi;

import com.google.gson.JsonElement;
import com.palantir.javapoet.*;
import moe.nea.frobjson.generator.SchemaType;
import moe.nea.frobjson.generator.TypeUtils;
import moe.nea.frobjson.openapi.Operation;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;

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

	public TypeSpec buildRecordBody() {
		return TypeSpec.recordBuilder("Body")
			.addModifiers(Modifier.PUBLIC)
			.recordConstructor(MethodSpec.constructorBuilder()
				.addParameter(fieldType(), "body")
				.build())
			.addSuperinterface(ParameterizedTypeName
				.get(ClassName.get(Operation.JsonBody.class), schema().typeName()))
			.addMethod(MethodSpec.methodBuilder("asJson")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.returns(TypeUtils.required(required(), ClassName.get(JsonElement.class)))
				.addStatement("return $L", schema().accessSerialize("this.body()"))
				.build())
			.build();
	}
}
