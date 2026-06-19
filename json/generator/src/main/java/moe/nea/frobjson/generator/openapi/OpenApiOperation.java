package moe.nea.frobjson.generator.openapi;

import com.google.gson.JsonElement;
import com.palantir.javapoet.*;
import moe.nea.frobjson.generator.GenerationContext;
import moe.nea.frobjson.generator.SchemaStringType;
import moe.nea.frobjson.generator.TypeUtils;
import moe.nea.frobjson.internal.OperationUtil;
import moe.nea.frobjson.openapi.Operation;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record OpenApiOperation(
	ClassName clsName,
	String method,
	String path,
	@Nullable String operationId,
	@Nullable String description,
	@Nullable String summary,
	@Nullable OpenApiRequestBody requestBody,
	List<OpenApiParameter> parameters,
	Map<Integer, OpenApiResponse> responses
) {

	public TypeName bodyType() {
		if (requestBody == null)
			return ClassName.get(Operation.EmptyBody.class);
		return clsName.nestedClass("Body");
	}

	public @Nullable OpenApiResponse successResponse() {
		return responses.get(200);
	}

	public TypeName successTyp() {
		var succ = successResponse();
		return succ != null ? succ.schema().typeName() : responsesTyp();
	}

	public ClassName responsesTyp() {
		return clsName.nestedClass("Response");
	}

	public JavaFile emitJavaFile(GenerationContext ctx) {
		var operationCls = TypeSpec.classBuilder(clsName)
			.addAnnotation(NullMarked.class)
			.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

		if (summary != null)
			operationCls.addJavadoc("$L\n<br>\n", summary);
		if (description != null)
			operationCls.addJavadoc("$L", TypeUtils.formatJavadoc(description));

		var responsesTyp = responsesTyp();
		var parametersTyp = clsName.nestedClass("Parameters");

		TypeName bodyType = bodyType();
		if (requestBody != null) {
			operationCls.addType(requestBody.buildRecordBody());
		}

		var parameterCls = buildParameterClass(parametersTyp);


		if (responses.isEmpty()) {
			operationCls
				.addType(TypeSpec.recordBuilder(responsesTyp.simpleName())
					.addModifiers(Modifier.PUBLIC)
					.addMethod(MethodSpec
						.methodBuilder("fromStatus")
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
						.addParameter(int.class, "statusCode")
						.addParameter(JsonElement.class, "json") // TODO: support non json responses
						.returns(responsesTyp)
						.addStatement("return new $T()", responsesTyp)
						.build())
					.build());
		} else if (responses.size() == 1) {
			var e = responses.entrySet().iterator().next();
			operationCls.addType(buildResponse(e.getKey(), e.getValue(), responsesTyp, null));
		} else {
			operationCls
				.addTypes(responses.entrySet().stream().map(responseE ->
					buildResponse(responseE.getKey(), responseE.getValue(), clsName.nestedClass("Status" + responseE.getKey()), responsesTyp)).toList())
				.addType(buildResponseSuperclass(responsesTyp, clsName));
		}

		operationCls
			.addType(parameterCls)
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
				.build())
			.addMethod(buildPathGenerator(parametersTyp))
			.addMethod(MethodSpec
				.methodBuilder("path")
				.addModifiers(Modifier.PUBLIC)
				.returns(String.class)
				.addAnnotation(Override.class)
				.addStatement("return $S", path)
				.build())
			.addMethod(MethodSpec
				.methodBuilder("method")
				.addModifiers(Modifier.PUBLIC)
				.returns(String.class)
				.addAnnotation(Override.class)
				.addStatement("return $S", method)
				.build())
			.addSuperinterface(ParameterizedTypeName.get(
				ClassName.get(Operation.class),
				parametersTyp,
				bodyType,
				responsesTyp));
		return JavaFile.builder(clsName.packageName(), operationCls.build()).build();
	}

	private TypeSpec buildResponseSuperclass(ClassName responsesTyp, ClassName clsName) {
		var typ = TypeSpec
			.interfaceBuilder(responsesTyp.simpleName())
			.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.SEALED)
			.addPermittedSubclasses(
				responses.keySet().stream()
					.map(it -> clsName.nestedClass("Status" + it))
					.toList())
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
				.addStatement("default -> throw new $T($S + statusCode)", RuntimeException.class, "Unknown status code: ")
				.endControlFlow("")
				.build());

		var succ = successResponse();
		if (succ != null)
			typ.addMethod(MethodSpec.methodBuilder("asSuccess")
				.returns(succ.schema().typeName())
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.build());

		return typ.build();
	}

	private MethodSpec buildPathGenerator(TypeName parameters) {
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
		return MethodSpec.methodBuilder("path")
			.returns(String.class)
			.addModifiers(Modifier.PUBLIC)
			.addParameter(parameters, "parameters")
			.addAnnotation(Override.class)
			.addStatement("return $L", builder.build())
			.build();

	}

	private TypeSpec buildResponse(
		int statusCode,
		OpenApiResponse response, ClassName clsName,
		@Nullable ClassName superTyp) {
		var builder = TypeSpec
			.recordBuilder(clsName.simpleName())
			.recordConstructor(MethodSpec.constructorBuilder()
				.addParameter(response.schema().typeName(), "body")
				.build())
			.addModifiers(Modifier.PUBLIC)
			.addMethod(MethodSpec
				.methodBuilder("fromStatus")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addParameter(int.class, "statusCode")
				.addParameter(JsonElement.class, "json") // TODO: support non json responses
				.returns(clsName)
				.beginControlFlow("if (statusCode != $L)", statusCode)
				.addStatement("throw new $T($S + statusCode)", RuntimeException.class, "Unknown status code: ")
				.endControlFlow()
				.addStatement("return $T.from(json)", clsName)
				.build())
			.addMethod(MethodSpec.methodBuilder("from")
				.returns(clsName)
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addParameter(JsonElement.class, "json")
				.addStatement("return new $T($L)", clsName, response.schema().accessDeserialize("json"))
				.build());
		if (superTyp != null)
			builder.addSuperinterface(superTyp);

		var succ = this.successResponse();
		if (succ != null) {
			var method = statusCode == 200
				? MethodSpec.methodBuilder("asSuccess")
				.returns(succ.schema().typeName())
				.addModifiers(Modifier.PUBLIC)
				.addStatement("return this.body()")
				: MethodSpec.methodBuilder("asSuccess")
				.returns(succ.schema().typeName())
				.addModifiers(Modifier.PUBLIC)
				.addStatement("throw $T.errorForResponse(this)", OperationUtil.class);
			if (superTyp != null)
				method.addAnnotation(Override.class);
			builder.addMethod(method.build());
		}
		return builder.build();
	}

	private TypeSpec buildParameterClass(ClassName parametersTyp) {
		var groupedPar = parameters.stream().collect(Collectors.groupingBy(OpenApiParameter::in));


		var parameterCls = TypeSpec.classBuilder(parametersTyp.simpleName())
			.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
			.addSuperinterface(Operation.Parameters.class);

		if (groupedPar.get(OpenApiParameter.In.QUERY) != null) {
			parameterCls.addField(FieldSpec
					.builder(TypeUtils.MAP_STR_STR, "$queryParameters")
					.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
					.initializer("new $T<>()", LinkedHashMap.class)
					.build())
				.addMethod(MethodSpec.methodBuilder("queryParameters")
					.addModifiers(Modifier.PUBLIC)
					.addAnnotation(Override.class)
					.returns(TypeUtils.MAP_STR_STR)
					.addStatement("return this.$L", "$queryParameters")
					.build());
		}
		if (groupedPar.get(OpenApiParameter.In.HEADER) != null) {
			parameterCls.addField(FieldSpec
				.builder(TypeUtils.MAP_STR_STR, "$headers")
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
				.initializer("new $T<>()", LinkedHashMap.class)
				.build());
		}
		if (groupedPar.get(OpenApiParameter.In.PATH) != null) {
			parameterCls.addMethod(MethodSpec.methodBuilder("pathParameter")
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
				.build());
		}

		parameterCls
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
							case QUERY -> CodeBlock.of("this.$L.put($S, $L);\n", "$queryParameters", it.name(),
								it.schema().unlazy() instanceof SchemaStringType
									? it.fieldName()
									: CodeBlock.of("($L).getAsString()", it.schema().accessSerialize(it.fieldName())));
							case HEADER ->
								CodeBlock.of("this.$L.put($S, $L);\n", "$headers", it.name(), it.fieldName());
						})
						.collect(CodeBlock.joining("")))
				.build())
			.addFields(groupedPar.getOrDefault(OpenApiParameter.In.PATH, List.of())
				.stream()
				.map(it -> FieldSpec
					.builder(it.type(), it.fieldName())
					.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
					.build())
				.toList())

			.addMethods(parameters
				.stream()
				.map(it -> {
					var method = MethodSpec
						.methodBuilder(it.fieldName())
						.addModifiers(Modifier.PUBLIC)
						.returns(it.in() == OpenApiParameter.In.PATH ? it.type() : TypeUtils.required(it.required(), ClassName.get(String.class)));
					switch (it.in()) {
						case HEADER -> method.addStatement("return this.$L.get($S)", "$headers", it.name());
						case QUERY -> method.addStatement("return this.$L.get($S)", "$queryParameters", it.name());
						case PATH -> method.addStatement("return this.$L", it.name());
					}
					return method.build();
				})
				.toList());
		return parameterCls.build();
	}

}
