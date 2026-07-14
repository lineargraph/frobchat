package moe.nea.frobjson.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.*;
import moe.nea.frobjson.internal.JsonUtil;
import moe.nea.frobjson.internal.SchemaObject;
import moe.nea.frobjson.internal.StreamUtil;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExtendedType implements Generatable {
	private final ClassName className;
	private final List<SchemaExtensionObjectType> types;
	private final String typeKey;
	private final SchemaObjectType baseType;

	public ExtendedType(
		ClassName className,
		List<SchemaExtensionObjectType> types,
		String typeKey,
		SchemaObjectType baseType) {
		this.className = className;
		this.types = types;
		this.typeKey = typeKey;
		this.baseType = baseType;
	}

	public static void generate(GenerationContext ctx, JsonElement extendedType) {
		var object = extendedType.getAsJsonObject();
		var title = object.get("extendsType").getAsString();
		var extensionType = ClassName.get(ctx.modelPackageName, ctx.typeNames.allocateName(title + " ext"));
		var typeKey = object.get("typeKey").getAsString();
		var baseSchema = (SchemaObjectType) Objects.requireNonNull(ctx.typeByTitle(title), "Could not find extensible type: " + title).unlazy(); // TODO: unlazy later on
		var names = new NameCollection(true);
		var types = JsonUtil.streamEntries(object.get("types").getAsJsonObject())
			.map(entry -> {
					var variantName = extensionType.nestedClass(names.allocateName(entry.getKey()));
					var variantType = new SchemaExtensionObjectType(ctx, (JsonObject) entry.getValue(), variantName, baseSchema, typeKey, entry.getKey());
					ctx.addSchema(variantType);
					return variantType;
				}
			).toList();
		ctx.enqueue(new ExtendedType(extensionType, types, typeKey, baseSchema));
	}

	@Override
	public List<? extends JavaFile> emitFiles() {
		var cls = TypeSpec.classBuilder(className)
			.addModifiers(Modifier.PUBLIC)
			.addMethod(downcastMethod())
			.addTypes(StreamUtil.iterable(types.stream().map(SchemaObjectType::buildClass).map(TypeSpec.Builder::build)));
		return List.of(JavaFile.builder(className.packageName(), cls.build()).build());
	}

	private MethodSpec downcastMethod() {
		var typeProp = baseType.indexedProperties.get(typeKey);
		return MethodSpec.methodBuilder("downcast")
			.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
			.returns(baseType.typeName())
			.addParameter(baseType.typeName(), "base")
			.beginControlFlow("switch (base.$L())", typeProp.fieldName())
			.addStatement(types.stream()
				.map(it-> CodeBlock.of("case $S:\nreturn $T.fromJson(base.asJson())", it.typeId(), it.typeName()))
				.collect(CodeBlock.joining(";\n")))
			.addStatement("default: return base")
			.endControlFlow()
			.build();
	}
}
