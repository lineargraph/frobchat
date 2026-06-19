package moe.nea.frobjson.generator.openapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palantir.javapoet.*;
import moe.nea.frobjson.generator.GenerationContext;
import moe.nea.frobjson.generator.NameCollection;
import moe.nea.frobjson.generator.TypeUtils;
import moe.nea.frobjson.internal.JsonUtil;
import moe.nea.frobjson.internal.StreamUtil;
import moe.nea.frobjson.openapi.Operation;
import moe.nea.frobjson.openapi.client.ApiClient;
import moe.nea.frobjson.openapi.client.ApiExecutor;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static moe.nea.frobjson.internal.JsonUtil.jsonObjectOrEmpty;

public class OpenApiSupport {
	public static Stream<JavaFile> generateAllSchemasFromOpenApi(
		GenerationContext ctx, JsonElement openApiDoc
	) {
		var clientName = ClassName.get(ctx.operationPackageName, ctx.operationTypeNames.allocateName("Client"));
		var operations = JsonUtil.streamEntriesOrEmpty(openApiDoc.getAsJsonObject().get("paths"))
			.flatMap(path -> JsonUtil.streamEntriesOrEmpty(path.getValue())
				.map(operation -> {
					var fieldNames = new NameCollection(false);
					var operationObj = operation.getValue().getAsJsonObject();
					var operationId = JsonUtil.getStringOrNull(operationObj.get("operationId"));
					var operationName =
						operationId != null
							? operationId
							: path.getKey();
					var responses = JsonUtil.streamEntriesOrEmpty(operationObj.get("responses"))
						.map(status -> {
							var statusObj = status.getValue().getAsJsonObject();
							var description = JsonUtil.getStringOrNull(statusObj.get("description"));
							var json = jsonObjectOrEmpty(statusObj.get("content")).get("application/json");
							if (json != null) {
								var statusSuffix = (status.getKey().equals("200") ? "" : status.getKey());
								var responseSchema = ctx.getSchemaForProperty(operationName + statusSuffix, json.getAsJsonObject().get("schema"), null);
								return Map.entry(Integer.parseInt(status.getKey()), new OpenApiResponse(description, responseSchema));
							}
							// TODO: other response content types
							return null;
						})
						.filter(Objects::nonNull)
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
					var parameters = JsonUtil.streamOrEmpty(operationObj.get("parameters"))
						.map(JsonElement::getAsJsonObject)
						.map(parameter -> {
							var name = parameter.get("name").getAsString();
							return new OpenApiParameter(
								name,
								fieldNames.allocateName(name),
								JsonUtil.getStringOrNull(parameter.get("description")),
								OpenApiParameter.In.valueOf(parameter.get("in").getAsString().toUpperCase(Locale.ROOT)),
								JsonUtil.getBooleanOrFalse(parameter.get("required")),
								ctx.getSchemaForProperty(name, parameter.get("schema"), null) // TODO: change the parent somehow
							);
						})
						.toList();
					var requestBodyObj = (JsonObject) operationObj.get("requestBody");
					OpenApiRequestBody requestBody = null;
					if (requestBodyObj != null) {
						var requestSchema =
							jsonObjectOrEmpty(jsonObjectOrEmpty(requestBodyObj.get("content"))
								.get("application/json"))
								.get("schema");
						if (requestSchema != null)
							requestBody = new OpenApiRequestBody(
								JsonUtil.getBooleanOrFalse(requestBodyObj.get("required")),
								ctx.getSchemaForProperty(operationName, requestSchema, null)
							);
					}
					return new OpenApiOperation(
						ClassName.get(ctx.operationPackageName, ctx.operationTypeNames.allocateName(operationName)),
						operation.getKey(),
						path.getKey(),
						operationId,
						JsonUtil.getStringOrNull(operationObj.get("description")),
						JsonUtil.getStringOrNull(operationObj.get("summary")),
						requestBody,
						parameters,
						responses
					);
				})).toList();
		return Stream.concat(
			Stream.of(createClient(clientName, operations)),
			operations.stream().map(it -> it.emitJavaFile(ctx)));
	}

	private static JavaFile createClient(ClassName clientName, List<OpenApiOperation> operations) {
		var typ = TypeSpec.classBuilder(clientName)
			.addModifiers(Modifier.PUBLIC)
			.superclass(ApiClient.class)
			.addAnnotation(TypeUtils.buildSuppressWarnings(Stream.of("unused")))
			.addMethod(MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ApiExecutor.class, "executor")
				.addStatement("super(executor)")
				.build())
			.addMethods(StreamUtil.iterable(operations.stream()
				.map(operation -> MethodSpec.methodBuilder(operation.operationId())
					.addModifiers(Modifier.PUBLIC)
					.returns(ParameterizedTypeName.get(ClassName.get(CompletableFuture.class), operation.responsesTyp()))
					.addParameters(StreamUtil.iterable(operation.parameters()
						.stream()
						.map(par -> ParameterSpec.builder(par.type(), par.fieldName()).build())))
					.addParameter(operation.bodyType(), "body")
					.addStatement(CodeBlock.of("return this.executor().executeOperation($T.INSTANCE,\n$L,\n$L)",
						operation.clsName(),
						CodeBlock.of("new $T.Parameters($L)",
							operation.clsName(),
							operation.parameters()
								.stream()
								.map(it -> CodeBlock.of("$L", it.fieldName()))
								.collect(CodeBlock.joining(",\n"))),
						"body"
					))
					.build())))
			.addMethods(StreamUtil.iterable(operations.stream()
				.filter(it -> it.requestBody() == null)
				.map(operation -> MethodSpec.methodBuilder(operation.operationId())
					.addModifiers(Modifier.PUBLIC)
					.returns(ParameterizedTypeName.get(ClassName.get(CompletableFuture.class), operation.responsesTyp()))
					.addParameters(StreamUtil.iterable(operation.parameters()
						.stream()
						.map(par -> ParameterSpec.builder(par.type(), par.fieldName()).build())))
					.addStatement(CodeBlock.of("return this.$L($L)",
						operation.operationId(),
						Stream.concat(operation.parameters()
							.stream()
							.map(it -> CodeBlock.of("$L", it.fieldName())),
							Stream.of(CodeBlock.of("$T.EMPTY_BODY", Operation.class))
						).collect(CodeBlock.joining(",\n"))))
					.build())));

		return JavaFile.builder(clientName.packageName(), typ.build()).build();
	}
}
