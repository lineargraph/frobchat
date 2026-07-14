package moe.nea.frobjson.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.*;
import moe.nea.frobjson.internal.JsonUtil;
import moe.nea.frobjson.internal.SchemaAllOf;
import moe.nea.frobjson.internal.StreamUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.util.List;

public class SchemaAllOfType implements SchemaType {
	private final String name;
	private final List<Variant> variants;
	private final ClassName typeName;

	record Variant(
		SchemaType type,
		String fieldName
	) {
	}

	public SchemaAllOfType(GenerationContext context, String name, List<SchemaType> aspects) {
		this.name = name;
		this.typeName = ClassName.get(context.modelPackageName, name);
		var names = new NameCollection(false);
		this.variants = aspects.stream()
			.map(it -> new Variant(it, names.allocateName(it.name())))
			.toList();
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
	public List<? extends JavaFile> emitFiles() {
		var cls = TypeSpec.classBuilder(typeName)
			.addAnnotation(NullMarked.class)
			.addSuperinterface(SchemaAllOf.class)
			.addFields(variants
				.stream()
				.map(it -> FieldSpec.builder(it.type().typeName(), it.fieldName(), Modifier.PRIVATE, Modifier.FINAL)
					.build())
				.toList())
			.addMethod(MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameters(StreamUtil.iterable(variants.stream()
					.map(it -> ParameterSpec.builder(it.type().typeName(), it.fieldName()).build())))
				.addCode(variants.stream().map(it -> CodeBlock.of("this.$L = $L;", it.fieldName(), it.fieldName())).collect(CodeBlock.joining("\n")))
				.build())
			.addField(FieldSpec.builder(ClassName.get(JsonElement.class).annotated(AnnotationSpec.builder(Nullable.class).build()), "$json", Modifier.PRIVATE).build())
			.addMethod(MethodSpec.methodBuilder("fromJson")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.returns(typeName)
				.addParameter(JsonElement.class, "$json")
				.addStatement("$T $L = new $T($L)",
					typeName, "$constructed",
					typeName,
					variants
						.stream()
						.map(it -> CodeBlock.builder()
							.add("$L", it.type().accessDeserialize("$json"))
							.build())
						.collect(CodeBlock.joining(",\n"))
				)
				.addStatement("$L.$L = $L", "$constructed", "$json", "$json")
				.addStatement("return $L", "$constructed")
				.build())
			.addMethod(MethodSpec.methodBuilder("toJson")
				.addModifiers(Modifier.PUBLIC)
				.returns(JsonElement.class)
				.beginControlFlow("if (this.$L == null)", "$json")
				.addStatement("return this.$L = this.generateJson()", "$json")
				.endControlFlow()
				.addStatement("return this.$L", "$json")
				.build())
			.addMethod(MethodSpec.methodBuilder("generateJson")
				.addModifiers(Modifier.PUBLIC)
				.returns(JsonElement.class)
				.addStatement("$T $L = new $T()", JsonObject.class, "$json", JsonObject.class)
				.addCode(variants
					.stream()
					.map(it -> CodeBlock.builder()
						.addStatement("$L = $T.mergeVariant($L, $T.bind(this.$L, $L))",
							"$json",
							JsonUtil.class,
							"$json",
							JsonUtil.class,
							it.fieldName(),
							it.type().serializeLambda("$field"))
						.build())
					.collect(CodeBlock.joining("\n")))
				.addStatement("return $L", "$json")
				.build())
			.addMethods(variants
				.stream()
				.map(it -> MethodSpec.methodBuilder(it.fieldName())
					.returns(it.type().typeName())
					.addModifiers(Modifier.PUBLIC)
					.addStatement("return this.$L", it.fieldName())
					.build())
				.toList());
		return List.of(JavaFile.builder(typeName.packageName(), cls.build()).build());
	}

	@Override
	public CodeBlock accessDeserialize(String jsonVariable) {
		return null;
	}

	@Override
	public CodeBlock accessSerialize(String sourceVariable) {
		return null;
	}
}
