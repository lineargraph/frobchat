package moe.nea.frobjson.generator;

import com.google.gson.JsonElement;
import com.palantir.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.List;

public record TaggedTypes(ClassName typeName, String name, List<TaggedType> types) implements Generatable {

	public static void generate(GenerationContext ctx, JsonElement jsonElement) {
		var json = jsonElement.getAsJsonObject();
		assert json.get("type").getAsString().equals("tagged-types");
		var name = json.get("name").getAsString();

		var subNameAlloc = new NameCollection(true);
		var types = json.getAsJsonObject("by-tag").entrySet().stream()
			.map(entry -> {
				var id = entry.getKey();
				var typeJson = entry.getValue();
				var childName = subNameAlloc.allocateName(id);
				var type = ctx.getSchemaForProperty(id, typeJson, null);
				return new TaggedType(childName, id, type);
			}).toList();
		var typeName = ctx.typeNames.allocateName(name);
		ctx.enqueue(new TaggedTypes(ClassName.get(ctx.modelPackageName, typeName), name, types));
	}

	@Override
	public List<? extends JavaFile> emitFiles() {
		return List.of(JavaFile.builder(typeName.packageName(), buildDispatcher()).build());
	}

	private TypeSpec buildDispatcher() {
		var spec = TypeSpec.interfaceBuilder(typeName)
			.addModifiers(Modifier.PUBLIC, Modifier.SEALED);
		for (var type : types) {
			var childType = typeName.nestedClass(type.childName);
			spec.addPermittedSubclass(childType);
			spec.addType(TypeSpec.recordBuilder(childType)
				.addSuperinterface(typeName)
				.addModifiers(Modifier.STATIC, Modifier.PUBLIC)
				.recordConstructor(MethodSpec.constructorBuilder()
					.addParameter(type.schemaType.typeName(), "value")
					.build())
				.addMethod(MethodSpec.methodBuilder("fromJson")
					.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
					.addParameter(JsonElement.class, "json")
					.returns(childType)
					.addStatement("return new $T($T.fromJson(json))", childType, type.schemaType.typeName())
					.build())
				.build());
		}
		spec.addMethod(MethodSpec.methodBuilder("fromJson")
			.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
			.returns(typeName)
			.addParameter(String.class, "type")
			.addParameter(JsonElement.class, "json")
			.beginControlFlow("switch (type)")
			.addCode(this.types.stream()
				.map(taggedType -> CodeBlock.of("case $S: return $T.fromJson(json);", taggedType.id, typeName.nestedClass(taggedType.childName())))
				.collect(CodeBlock.joining("\n")))
			.endControlFlow()
			.addStatement("return null")
			.build());
		return spec.build();
	}

	record TaggedType(
		String childName,
		String id,
		SchemaType schemaType
	) {
	}
}
