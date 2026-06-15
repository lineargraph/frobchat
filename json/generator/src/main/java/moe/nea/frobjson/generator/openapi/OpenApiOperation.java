package moe.nea.frobjson.generator.openapi;

import com.google.gson.JsonElement;
import com.palantir.javapoet.*;
import moe.nea.frobjson.generator.GenerationContext;
import moe.nea.frobjson.generator.NameCollection;
import moe.nea.frobjson.generator.SchemaStringType;
import moe.nea.frobjson.generator.TypeUtils;
import moe.nea.frobjson.openapi.Operation;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public record OpenApiOperation(
	String method,
	String path,
	String operationId,
	String description,
	String summary,
	@Nullable OpenApiRequestBody requestBody,
	List<OpenApiParameter> parameters,
	Map<Integer, OpenApiResponse> responses
) {

	public JavaFile emitJavaFile(GenerationContext ctx) {
		String packageName = ctx.operationPackageName;
		var clsName = ClassName.get(packageName, ctx.operationTypeNames.allocateName(operationId));
		var operationCls = TypeSpec.classBuilder(clsName)
			.addAnnotation(NullMarked.class)
			.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		var anyPath = parameters.stream().anyMatch(it -> it.in() == OpenApiParameter.In.PATH);
		var responsesTyp = clsName.nestedClass("Responses");
		var parametersTyp = clsName.nestedClass("Parameters");
		var mapStrStr = ParameterizedTypeName.get(Map.class, String.class, String.class);
		TypeName bodyType = ClassName.get(Operation.EmptyBody.class);
		if (requestBody != null) {
			bodyType = clsName.nestedClass("Body");

			var bodyCls = TypeSpec.recordBuilder("Body")
				.addModifiers(Modifier.PUBLIC)
				.recordConstructor(MethodSpec.constructorBuilder()
					.addParameter(requestBody.fieldType(), "body")
					.build())
				.addSuperinterface(ParameterizedTypeName
					.get(ClassName.get(Operation.JsonBody.class), requestBody.schema().typeName()))
				.addMethod(MethodSpec.methodBuilder("asJson")
					.addModifiers(Modifier.PUBLIC)
					.returns(TypeUtils.required(requestBody.required(), ClassName.get(JsonElement.class)))
					.addStatement("return $L", requestBody.schema().accessSerialize("this.body()"))
					.build());
			operationCls.addType(bodyCls.build());
		}
		var parameterCls = TypeSpec.classBuilder(parametersTyp.simpleName())
			.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
			.addSuperinterface(Operation.Parameters.class)
			.addField(FieldSpec
				.builder(mapStrStr, "$queryParameters")
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
				.initializer("new $T<>()", LinkedHashMap.class)
				.build())
			.addField(FieldSpec
				.builder(mapStrStr, "$headers")
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
				.initializer("new $T<>()", LinkedHashMap.class)
				.build())
			.addMethod(MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameters(parameters.stream()
					.map(it -> ParameterSpec
						.builder(it.type(), it.fieldName())
						.build())
					.toList())
				.addCode(
					parameters.stream()
						.map(it -> switch (it.in()) {
							case PATH -> CodeBlock.of("this.$L = $L;\n", it.fieldName(), it.fieldName());
							case QUERY ->
								CodeBlock.of("this.$L.put($S, $L);\n", "$queryParameters", it.name(), it.schema().unlazy() instanceof SchemaStringType ? it.fieldName() : CodeBlock.of("($L).getAsString()", it.schema().accessSerialize(it.fieldName())));
							case HEADER ->
								CodeBlock.of("this.$L.put($S, $L);\n", "$headers", it.name(), it.fieldName());
						})
						.collect(CodeBlock.joining(""))
				)
				.build())
			.addMethod(MethodSpec.methodBuilder("queryParameters")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.returns(mapStrStr)
				.addStatement("return this.$L", "$queryParameters")
				.build())
			.addFields(parameters
				.stream()
				.filter(it -> it.in() == OpenApiParameter.In.PATH)
				.map(it -> FieldSpec
					.builder(it.type(), it.name())
					.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
					.build())
				.toList())
			.addMethods(anyPath ? List.of(MethodSpec.methodBuilder("pathParameter")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.addParameter(String.class, "name")
				.returns(String.class)
				.beginControlFlow("return switch (name)")
				.addCode(
					parameters.stream()
						.filter(it -> it.in() == OpenApiParameter.In.PATH)
						.map(it -> CodeBlock.of("case $S -> this.$L;\n", it.name(), it.name()))
						.collect(CodeBlock.joining(""))
				)
				.addStatement("default -> $T.super.pathParameter($L)", Operation.Parameters.class, "name")
				.endControlFlow("")
				.build()) : List.of())

			.addMethods(parameters
				.stream()
				.filter(it -> it.in() != OpenApiParameter.In.HEADER)
				.map(it -> {
					var method = MethodSpec
						.methodBuilder(it.name())
						.addModifiers(Modifier.PUBLIC)
						.returns(it.in() == OpenApiParameter.In.QUERY ? TypeUtils.required(it.required(), ClassName.get(String.class)) : it.type());
					switch (it.in()) {
						case QUERY -> {
							method.addStatement("return this.$L.get($S)", "$queryParameters", it.name());
						}
						case PATH -> {
							method.addStatement("return this.$L", it.name());
						}
					}
					return method.build();
				})
				.toList());

		for (var responseE : responses.entrySet()) {
			var response = responseE.getValue();
			var self = clsName.nestedClass("Status" + responseE.getKey());
			operationCls.addType(TypeSpec.recordBuilder(self.simpleName())
				.recordConstructor(MethodSpec.constructorBuilder()
					.addParameter(response.schema().typeName(), "body")
					.build())
				.addSuperinterface(responsesTyp)
				.addModifiers(Modifier.PUBLIC)
				.addMethod(MethodSpec.methodBuilder("from")
					.returns(self)
					.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
					.addParameter(JsonElement.class, "json")
					.addStatement("return new $T($L)", self, response.schema().accessDeserialize("json"))
					.build())
				.build());
		}
		operationCls.addType(TypeSpec.recordBuilder("StatusUnknown")
			.recordConstructor(MethodSpec.constructorBuilder()
				.addParameter(int.class, "statusCode")
				.build())
			.addSuperinterface(responsesTyp)
			.addModifiers(Modifier.PUBLIC)
			.build());
		operationCls
			.addMethod(MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PRIVATE).build())
			.addField(FieldSpec.builder(clsName, "INSTANCE")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
				.initializer("new $T()", clsName)
				.build())
			.addMethod(MethodSpec.methodBuilder("fromResponse")
				.returns(responsesTyp)
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(int.class, "statusCode")
				.addParameter(JsonElement.class, "element")
				.addStatement("return $T.fromStatus(statusCode, element)", responsesTyp)
				.build());
		{
			CodeBlock.Builder builder = CodeBlock.builder();
			var matcher = Pattern.compile("\\{([^}]+)\\}").matcher(path);
			var lastAppendPosition = 0;
			while (matcher.find()) {
				if (!builder.isEmpty())
					builder.add("\n + ");
				builder.add("$S + $L.pathParameter($S)", path.substring(lastAppendPosition, matcher.start()), "parameters", matcher.group(1)); // TODO: map group -> fieldName
				lastAppendPosition = matcher.end();
			}
			if (!builder.isEmpty())
				builder.add("\n + ");
			builder.add("$S", path.substring(lastAppendPosition));
			operationCls
				.addMethod(MethodSpec.methodBuilder("path")
					.returns(String.class)
					.addModifiers(Modifier.PUBLIC)
					.addParameter(parametersTyp, "parameters")
					.addAnnotation(Override.class)
					.addStatement("return $L", builder.build())
					.build());
		}
		operationCls.addType(TypeSpec
			.interfaceBuilder(responsesTyp.simpleName())
			.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.SEALED)
			.addPermittedSubclasses(
				responses.keySet().stream()
					.map(it -> clsName.nestedClass("Status" + it))
					.toList()
			)
			.addPermittedSubclass(clsName.nestedClass("StatusUnknown"))
			.addMethod(MethodSpec
				.methodBuilder("fromStatus")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addParameter(int.class, "statusCode")
				.addParameter(JsonElement.class, "json") // TODO: support non json responses
				.returns(responsesTyp)
				.beginControlFlow("return switch (statusCode)")
				.addCode(
					responses.keySet().stream()
						.map(integer -> CodeBlock.of("case $L -> $T.from($L);\n", integer, clsName.nestedClass("Status" + integer), "json"))
						.collect(CodeBlock.joining(""))
				)
				.addStatement("default -> new $T(statusCode)", clsName.nestedClass("StatusUnknown"))
				.endControlFlow("")
				.build())
			.build());

		operationCls.addMethod(MethodSpec
			.methodBuilder("path")
			.addModifiers(Modifier.PUBLIC)
			.returns(String.class)
			.addAnnotation(Override.class)
			.addStatement("return $S", path)
			.build());
		operationCls.addMethod(MethodSpec
			.methodBuilder("method")
			.addModifiers(Modifier.PUBLIC)
			.returns(String.class)
			.addAnnotation(Override.class)
			.addStatement("return $S", method)
			.build());
		operationCls.addType(parameterCls.build());
		operationCls.addSuperinterface(ParameterizedTypeName.get(
			ClassName.get(Operation.class),
			parametersTyp,
			bodyType,
			responsesTyp));
		return JavaFile.builder(clsName.packageName(),
			operationCls.build()).build();
	}

}
