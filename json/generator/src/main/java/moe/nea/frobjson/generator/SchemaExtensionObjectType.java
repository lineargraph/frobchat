package moe.nea.frobjson.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.*;
import moe.nea.frobjson.internal.JsonUtil;
import moe.nea.frobjson.internal.StreamUtil;

import javax.lang.model.element.Modifier;

public class SchemaExtensionObjectType extends SchemaObjectType {
	private final SchemaObjectType baseType;
	private final String typeKey;
	private final String typeId;

	public SchemaExtensionObjectType(
		GenerationContext context, JsonObject definition, ClassName typeName,
		SchemaObjectType baseType,
		String typeKey, String typeId) {
		super(context, definition, typeName);
		this.baseType = baseType;
		this.typeKey = typeKey;
		this.typeId = typeId;
	}

	@Override
	MethodSpec buildConstructor() {
		var constructor = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC)
			.addParameters(
				StreamUtil.iterable(baseType.properties
					.stream()
					.filter(prop -> !prop.propName().equals(typeKey))
					.map(prop -> ParameterSpec.builder(prop.fieldType(), prop.fieldName()).build())))
			.addStatement("super($L)",
				baseType.properties
					.stream()
					.map(prop -> prop.propName().equals(typeKey)
						? CodeBlock.of("$S", typeId)
						: CodeBlock.of("$L", prop.fieldName()))
					.collect(CodeBlock.joining(", ")));

		for (var prop : properties) {
			constructor.addParameter(prop.fieldType(), prop.fieldName())
				.addStatement("this.$L = $L", prop.fieldName(), prop.fieldName());
		}
		return constructor.build();

	}

	@Override
	public TypeSpec.Builder buildClass() {
		return super.buildClass()
			.addModifiers(Modifier.STATIC)
			.superclass(baseType.typeName);
	}

	@Override
	MethodSpec buildGenerateJson() {
		var encode = MethodSpec.methodBuilder("generateJson")
			.returns(JsonElement.class)
			.addAnnotation(Override.class)
//			.addAnnotation(buildSuppressWarnings(Stream.of("UnnecessaryLocalVariable", "Convert2MethodRef")))
			.addModifiers(Modifier.PUBLIC)
			.addStatement("$T $L = ($T) super.generateJson()", JsonObject.class, "$json", JsonObject.class)
//			.addStatement("$L.addProperty($S, $S)", "$json", typeKey, typeId)
			;
		for (var prop : properties) {
			encode.addStatement("""
				$T.acceptNullable(
				$L,
				it -> $L.add($S, it))""", JsonUtil.class, prop.serializerExpression("this"), "$json", prop.propName());
		}
		encode.addStatement("return $L", "$json");
		return encode.build();

	}
}
