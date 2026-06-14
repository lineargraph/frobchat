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
import java.util.stream.Stream;

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

	MethodSpec buildGenerateJson() {
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
					.addStatement("@$T($S) $T $L = this.$L", SuppressWarnings.class, "UnnecessaryLocalVariable", prop.fieldType()
						.withoutAnnotations(), "$field", prop.fieldName())
					.addStatement("$L.add($S, $L)", "$json", prop.propName(), prop.type().accessSerialize("$field"))
					.endControlFlow();
			}
			encode.addStatement("return $L", "$json");
		}
		return encode.build();
	}

	FieldSpec buildField(SchemaProperty prop) {
		var field = FieldSpec.builder(
			prop.fieldType(),
			prop.fieldName(),
			Modifier.PRIVATE,
			Modifier.FINAL
		);
		prop.type().decorateField(field);
		return field.build();
	}

	MethodSpec buildConstructor() {
		var constructor = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC);

		for (var prop : properties) {
			constructor.addParameter(prop.fieldType(), prop.fieldName())
				.addStatement("this.$L = $L", prop.fieldName(), prop.fieldName());
		}
		return constructor.build();
	}

	MethodSpec buildToString() {
		var toString = MethodSpec.methodBuilder("toString")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PUBLIC)
			.returns(String.class)
			.addCode("return $S\n", name + " { ");

		for (var prop : properties) {
			toString.addCode("    + $S + this.$L + $S\n", prop.propName() + "=", prop.fieldName(), ", ");
		}
		return toString
			.addStatement("    + $S", "... }")
			.build();
	}

	MethodSpec buildGetter(SchemaProperty prop) {
		return MethodSpec.methodBuilder(prop.fieldName())
			.addModifiers(Modifier.PUBLIC)
			.returns(prop.fieldType())
			.addStatement("return this.$L", prop.fieldName()).build();
	}

	MethodSpec buildDecode() {
		var decode = MethodSpec.methodBuilder("fromJson")
			.returns(typeName)
			.addModifiers(Modifier.STATIC, Modifier.PUBLIC)
			.addParameter(JsonElement.class, "$json");
		var jsonObjectName = "$json$object";
		decode.addStatement("$T $L = $L.getAsJsonObject()", JsonObject.class, jsonObjectName, "$json");
		for (var prop : properties) {
			decode.addStatement("$T $L", prop.fieldType().withoutAnnotations(), prop.fieldName())
				.beginControlFlow("")
				.addStatement("$T $L = $L.get($S)", JsonElement.class, "$jsonField", jsonObjectName, prop.propName());

			if (!prop.required()) {
				decode.beginControlFlow("if ($L == null)", "$jsonField")
					.addStatement("$L = null", prop.fieldName())
					.nextControlFlow("else")
					.addStatement("$L = $L", prop.fieldName(), prop.type().accessDeserialize("$jsonField"))
					.endControlFlow();
			} else {
				decode.addStatement("$L = $L", prop.fieldName(), prop.type().accessDeserialize("$jsonField"));
			}
			decode.endControlFlow();
		}
		decode
			.addCode("$T $L = new $T(\n", typeName, "$constructed", typeName)
			.addCode(
				properties.stream()
					.map(it -> CodeBlock.of("    $L", it.fieldName()))
					.collect(CodeBlock.joining(",\n")))
			.addStatement(")")
			.addStatement("$L.$L = $L", "$constructed", "$json", "$json")
			.addStatement("return $L", "$constructed");

		return decode.build();
	}

	MethodSpec buildAsJson() {
		return MethodSpec.methodBuilder("asJson")
			.addModifiers(Modifier.PUBLIC)
			.returns(JsonElement.class)
			.beginControlFlow("if (this.$L == null)", "$json")
			.addStatement("return this.$L = this.generateJson()", "$json")
			.endControlFlow()
			.addStatement("return this.$L", "$json")
			.build();
	}

	FieldSpec buildJsonField() {
		return FieldSpec
			.builder(TypeName.get(JsonElement.class)
				.annotated(AnnotationSpec.builder(Nullable.class).build()), "$json", Modifier.PRIVATE)
			.build();
	}

	AnnotationSpec buildSuppressWarnings(Stream<? extends String> warnings) {
		return AnnotationSpec.builder(SuppressWarnings.class)
			.addMember("value", warnings
				.map(it -> CodeBlock.of("$S", it))
				.collect(CodeBlock.joining(", ", "{", "}")))
			.build();
	}

	@Override
	public List<JavaFile> emitFiles() {
		var cls = TypeSpec.classBuilder(typeName)
			.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
			.addAnnotation(NullMarked.class)
			.addAnnotation(buildSuppressWarnings(Stream.of("unused")))
			.addField(buildJsonField())
			.addFields(properties.stream().map(this::buildField).toList())
			.addMethods(properties.stream().map(this::buildGetter).toList())
			.addMethod(buildConstructor())
			.addMethod(buildToString())
			.addMethod(buildGenerateJson())
			.addMethod(buildAsJson())
			.addMethod(buildDecode());

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
