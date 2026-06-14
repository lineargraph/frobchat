package moe.nea.frobjson.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.*;
import moe.nea.frobjson.internal.JsonUtil;
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
	List<SchemaProperty> properties;
	ClassName typeName;
	NameCollection fieldNames = new NameCollection(false);

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
		this.properties = JsonUtil.streamEntriesOrEmpty(definition.get("properties"))
			.map(prop -> new SchemaProperty(
				prop.getKey(),
				fieldNames.allocateName(prop.getKey()),
				context.getSchemaForProperty(prop.getKey(), prop.getValue(), this),
				requiredProps.contains(prop.getKey())
			)).toList();
	}

	@Override
	public List<JavaFile> emitFiles() {
		var nullable = AnnotationSpec.builder(Nullable.class).build();
		var cls = TypeSpec.classBuilder(typeName);
		cls.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		cls.addAnnotation(NullMarked.class)
			.addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
				.addMember("value", "$S", "unused").build());

		cls.addField(FieldSpec.builder(TypeName.get(JsonElement.class)
			.annotated(nullable), "$json", Modifier.PRIVATE).build());


		{
			var encode = MethodSpec.methodBuilder("generateJson")
				.returns(JsonElement.class)
				.addModifiers(Modifier.PUBLIC);
			if (properties.isEmpty()) {
				encode.addStatement("return new $T()", JsonObject.class);
			} else {
				encode.addStatement("$T $L = new $T()", JsonObject.class, "$json", JsonObject.class);
				for (var prop : properties) {
					if (!prop.required()) {
						encode.beginControlFlow("if (this.$L != null)", prop.fieldName());
					} else {
						encode.beginControlFlow("");
					}

					encode
						.addStatement("@$T($S) $T $L = this.$L", SuppressWarnings.class, "UnnecessaryLocalVariable", prop.fieldType().withoutAnnotations(), "$field", prop.fieldName())
						.addStatement("$L.add($S, $L)", "$json", prop.propName(), prop.type().accessSerialize("$field"))
						.endControlFlow();
				}
				encode.addStatement("return $L", "$json");
			}
			cls.addMethod(encode.build());
		}
		var decode = MethodSpec.methodBuilder("fromJson")
			.returns(typeName)
			.addModifiers(Modifier.STATIC, Modifier.PUBLIC)
			.addParameter(JsonElement.class, "$json");
		var jsonObjectName = "$json$object";
		decode.addStatement("$T $L = $L.getAsJsonObject()", JsonObject.class, jsonObjectName, "$json");

		for (var prop : properties) {
			var field = FieldSpec.builder(
				prop.fieldType(),
				prop.fieldName(),
				Modifier.PRIVATE,
				Modifier.FINAL
			);
			prop.type().decorateField(field);
			cls.addField(field.build());
		}

		{
			var constructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC);

			for (var prop : properties) {
				// Constructor
				constructor.addParameter(prop.fieldType(), prop.fieldName())
					.addStatement("this.$L = $L", prop.fieldName(), prop.fieldName());
			}
			cls.addMethod(constructor.build());
		}
		var constructorCall = CodeBlock.builder()
			.add("$T $L = new $T(\n", typeName, "$constructed", typeName);

		{
			var toString = MethodSpec.methodBuilder("toString")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(String.class)
				.addCode("return $S\n", name + " { ");

			for (var prop : properties) {
				toString.addCode("    + $S + this.$L + $S\n", prop.propName() + "=", prop.fieldName(), ", ");
			}
			cls.addMethod(toString
				.addStatement("    + $S", "... }")
				.build());
		}
		var propIter = properties.iterator();
		var hasAnyProps = false;
		while (propIter.hasNext()) {
			hasAnyProps = true;
			var prop = propIter.next();
			var isLast = !propIter.hasNext();
			var propName = prop.propName();
			var fieldName = prop.fieldName();
			var schemaType = prop.type();
			var fieldType = prop.fieldType();

			// Getter
			cls.addMethod(MethodSpec.methodBuilder(fieldName).addModifiers(Modifier.PUBLIC).returns(fieldType).addStatement("return this.$L", fieldName).build());

			// Decoding
			constructorCall.add("$L" + (isLast ? ")" : ",\n"), fieldName);
			decode.addStatement("$T $L", fieldType.withoutAnnotations(), fieldName)
				.beginControlFlow("")
				.addStatement("$T $L = $L.get($S)", JsonElement.class, "$jsonField", jsonObjectName, propName);

			if (!requiredProps.contains(propName)) {
				decode.beginControlFlow("if ($L == null)", "$jsonField")
					.addStatement("$L = null", fieldName)
					.nextControlFlow("else")
					.addStatement("$L = $L", fieldName, schemaType.accessDeserialize("$jsonField"))
					.endControlFlow();
			} else {
				decode.addStatement("$L = $L", fieldName, schemaType.accessDeserialize("$jsonField"));
			}
			decode.endControlFlow();
		}
		if (!hasAnyProps)
			constructorCall.add(")");
		decode.addStatement(constructorCall.build())
			.addStatement("$L.$L = $L", "$constructed", "$json", "$json")
			.addStatement("return $L", "$constructed");

		cls.addMethod(MethodSpec.methodBuilder("asJson")
			.addModifiers(Modifier.PUBLIC)
			.returns(JsonElement.class)
			.beginControlFlow("if (this.$L == null)", "$json")
			.addStatement("return this.$L = this.generateJson()", "$json")
			.endControlFlow()
			.addStatement("return this.$L", "$json")
			.build());
		cls.addMethod(decode.build());
//		cls.addMethod(MethodSpec.methodBuilder("shallowWithoutExtras").build())
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
	public CodeBlock accessDeserialize(String jsonVariable) {
		return CodeBlock.of("$T.fromJson($L)", typeName, jsonVariable);
	}

	@Override
	public CodeBlock accessSerialize(String sourceVariable) {
		return CodeBlock.of("$L.asJson()", sourceVariable);
	}
}
