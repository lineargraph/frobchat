package moe.nea.frobjson.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SchemaObjectType implements SchemaType {
	GenerationContext context;
	JsonObject definition;
	String name;
	Set<String> requiredProps;
	ClassName typeName;
	NameCollection fieldNames = new NameCollection();

	public SchemaObjectType(
		GenerationContext context,
		JsonObject definition,
		String propertyName,
		@Nullable SchemaType parent
	) {
		assert "object".equals(JsonUtil.getStringOrNull(definition.get("type")));
		this.definition = definition;
		this.context = context;
		this.name = context.deriveName(
			parent,
			JsonUtil.getStringOrNull(definition.get("title")),
			propertyName
		);
		this.typeName = ClassName.get(context.packageName, name);
		this.requiredProps = JsonUtil.streamOrEmpty(definition.get("required")).map(JsonElement::getAsString).collect(Collectors.toSet());
	}

	@Override
	public List<JavaFile> emitFiles() {
		var nullable = AnnotationSpec.builder(Nullable.class).build();
		var cls = TypeSpec.classBuilder(typeName);
		cls.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		cls.addAnnotation(NullMarked.class);

		cls.addField(FieldSpec.builder(TypeName.get(JsonElement.class)
			.annotated(nullable), "$json", Modifier.PRIVATE).build());

		var constructor = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC);
		var encode = MethodSpec.methodBuilder("generateJson")
			.returns(JsonElement.class)
			.addModifiers(Modifier.PUBLIC)
			.addStatement("$T $L = new $T()", JsonObject.class, "$json", JsonObject.class);
		var decode = MethodSpec.methodBuilder("fromJson")
			.returns(typeName)
			.addModifiers(Modifier.STATIC, Modifier.PUBLIC)
			.addParameter(JsonElement.class, "$json");
		var jsonObjectName = "$json$object";
		decode.addStatement("$T $L = $L.getAsJsonObject()", JsonObject.class, jsonObjectName, "$json");
		var constructorCall = CodeBlock.builder()
			.add("$T $L = new $T(\n", typeName, "$constructed", typeName);

		var propIter = definition.getAsJsonObject("properties").entrySet().iterator();
		while (propIter.hasNext()) {
			var prop = propIter.next();
			var isLast = !propIter.hasNext();
			var propName = prop.getKey();
			var fieldName = fieldNames.allocateName(propName);
			var schemaType = context.getSchemaForProperty(
				prop.getKey(),
				prop.getValue(),
				this
			);
			var fieldType = schemaType.typeName();
			if (!requiredProps.contains(propName)) {
				fieldType = fieldType.annotated(nullable);
			}
			var field = FieldSpec.builder(
				fieldType,
				fieldName,
				Modifier.PRIVATE,
				Modifier.FINAL
			);

			schemaType.decorateField(field);
			cls.addField(field.build());
			// Getter
			cls.addMethod(MethodSpec.methodBuilder(fieldName).addModifiers(Modifier.PUBLIC).returns(fieldType).addStatement("return this.$L", fieldName).build());

			// Constructor
			constructor.addParameter(fieldType, fieldName)
				.addStatement("this.$L = $L", fieldName, fieldName);

			// Encoding
			if (!requiredProps.contains(propName)) {
				encode.beginControlFlow("if (this.$L != null)", fieldName);
			} else {
				encode.beginControlFlow("");
			}
			encode
				.addStatement("$T $L", JsonElement.class, "$jsonField")
				.addCode(schemaType.accessSerialize("this." + fieldName, "$jsonField"))
				.addStatement("$L.add($S, $L)", "$json", propName, "$jsonField")
				.endControlFlow();

			// Decoding
			constructorCall.add("$L" + (isLast ? ")" : ",\n"), fieldName);
			decode.addStatement("$T $L", fieldType.withoutAnnotations(), fieldName)
				.beginControlFlow("")
				.addStatement("$T $L = $L.get($S)", JsonElement.class, "$jsonField", jsonObjectName, propName);
			if (!requiredProps.contains(propName)) {
				decode.beginControlFlow("if ($L == null)", "$jsonField")
					.addStatement("$L = null", fieldName)
					.nextControlFlow("else")
					.addCode(schemaType.accessDeserialize(fieldName, "$jsonField"))
					.endControlFlow();
			} else {
				decode.addCode(schemaType.accessDeserialize(fieldName, "$jsonField"));
			}
			decode.endControlFlow();
		}
		decode.addStatement(constructorCall.build())
			.addStatement("$L.$L = $L", "$constructed", "$json", "$json")
			.addStatement("return $L", "$constructed");

		encode.addStatement("return $L", "$json");

		cls.addMethod(encode.build());
		cls.addMethod(MethodSpec.methodBuilder("asJson")
			.addModifiers(Modifier.PUBLIC)
			.returns(JsonElement.class)
			.beginControlFlow("if (this.$L == null)", "$json")
			.addStatement("return this.$L = this.generateJson()", "$json")
			.endControlFlow()
			.addStatement("return this.$L", "$json")
			.build());
		cls.addMethod(decode.build());
		cls.addMethod(constructor.build());
		return List.of(JavaFile
			.builder(context.packageName, cls.build())
			.build());
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public TypeName typeName() {
		return typeName;
	}

	@Override
	public CodeBlock accessDeserialize(String destinationVariable, String jsonVariable) {
		return CodeBlock.of("$L = $T.fromJson($L);\n", destinationVariable, typeName, jsonVariable);
	}

	@Override
	public CodeBlock accessSerialize(String sourceVariable, String jsonVariable) {
		return CodeBlock.of("$L = $L.asJson();\n", jsonVariable, sourceVariable);
	}
}
