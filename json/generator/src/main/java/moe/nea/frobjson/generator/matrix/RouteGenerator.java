package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.*;
import moe.nea.frobjson.generator.NameCollection;
import moe.nea.frobjson.generator.TypeUtils;
import moe.nea.frobmatrix.api.ResponseHelpers;
import moe.nea.frobmatrix.api.Route;
import moe.nea.frobmatrix.http.UriBuilder;
import moe.nea.frobmatrix.util.Pair;
import tools.jackson.core.type.TypeReference;

import javax.lang.model.element.Modifier;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RouteGenerator implements ClassGenerator {
	public static final Pattern PATH_PATTERN = Pattern.compile("\\{([^}]+)\\}");

	// Route data
	private final String path;
	private final String method;
	public final RawOpenApiRoutes.Route route;

	// Type resolution data
	private final Map<String, String> parameterNameToField = new HashMap<>();
	private final Map<RawOpenApiRoutes.ResponseContent, TypeName> responseContentTypes = new HashMap<>();
	private TypeName requestBodyType;
	private final Map<RawOpenApiRoutes.Parameter, TypeName> parameterTypes = new LinkedHashMap<>();
	private TypeName successResponseType;

	// Shared state during generation
	private final NameCollection fieldNames = new NameCollection(NameCollection.NameType.FIELD_NAME, true);
	private final MethodSpec.Builder allParametersConstructor = MethodSpec.constructorBuilder()
		.addModifiers(Modifier.PUBLIC);
	private MethodSpec.Builder responseParser;

	public RouteGenerator(
		String path,
		String method,
		RawOpenApiRoutes.Route route
	) {
		this.path = path;
		this.method = method;
		this.route = route;
	}

	public List<Pair<String, TypeName>> getAllRequestArguments() {
		return Stream.concat(
			Stream.ofNullable(requestBodyType)
				.map(it -> Pair.of("requestBody", requestBodyType)),
			parameterTypes.entrySet()
				.stream()
				.map(it -> Pair.of(it.getKey().getName(), it.getValue()))
		).toList();
	}

	private void addBodyMethod(TypeSpec.Builder spec) {
		if (requestBodyType == null) return;
		allParametersConstructor
			.addParameter(requestBodyType, "requestBody")
			.addStatement("this.requestBody = requestBody");
		spec.addField(requestBodyType, "requestBody", Modifier.PRIVATE)
			.addMethod(MethodSpec.methodBuilder("requestBody")
				.returns(requestBodyType)
				.addStatement("return this.requestBody")
				.addModifiers(Modifier.PUBLIC)
				.build())
			.addMethod(MethodSpec.methodBuilder("contentType")
				.returns(String.class)
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addStatement("return $S", "application/json")
				.build())
			.addMethod(MethodSpec.methodBuilder("bodyPublisher")
				.returns(HttpRequest.BodyPublisher.class)
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.addStatement("return $T.publishJson(requestBody)", ResponseHelpers.class)
				.build());
	}

	private void addPathMethod(TypeSpec.Builder spec) {
		var method = MethodSpec.methodBuilder("appendUriTo")
			.addModifiers(Modifier.PUBLIC)
			.returns(UriBuilder.class)
			.addParameter(UriBuilder.class, "uriBuilder")
			.addAnnotation(Override.class);
		method.addCode("return uriBuilder");
		{
			var pathMatcher = PATH_PATTERN.matcher(path);
			int lastAppendPosition = 0;
			while (pathMatcher.find()) {
				var skipped = path.substring(lastAppendPosition, pathMatcher.start());
				if (!skipped.isEmpty()) {
					method.addCode(".rawPath($S)", skipped);
				}
				method.addCode(".pathComponent($L)", Objects.requireNonNull(parameterNameToField.get(pathMatcher.group(1))));
				lastAppendPosition = pathMatcher.end();
			}
			var rest = path.substring(lastAppendPosition);
			if (!rest.isEmpty()) {
				method.addCode(".rawPath($S)", rest);
			}
		}

		for (var parameter : route.getParameters()) {
			if (parameter.getIn() != RawOpenApiRoutes.ParameterLocation.QUERY) continue;
			method.addCode(".appendQueryParameter($S, $L)", parameter.getName(), Objects.requireNonNull(parameterNameToField.get(parameter.getName())));
		}

		method.addCode(";");
		spec.addMethod(method.build());
	}

	private void generateErrorResponse(TypeSpec.Builder spec, int statusCode, RawOpenApiRoutes.Response response) {
		for (var entry : response.getContent().entrySet()) {
			var contentType = entry.getKey();
			if (!contentType.equals("application/json"))
				throw new RuntimeException("Unhandled content type application/json");
			var responseContent = entry.getValue();
			var resultType = responseContentTypes.get(responseContent);
			boolean isError = statusCode != 200;
			if (!isError) {
				spec.superclass(ParameterizedTypeName.get(ClassName.get(Route.class), resultType));
				this.responseParser
					.returns(ParameterizedTypeName.get(ClassName.get(HttpResponse.BodySubscriber.class),
						ParameterizedTypeName.get(ClassName.get(Supplier.class), resultType)
					))
					.addStatement("case $L: return $T.parseJson(responseInfo, new $T<$T>() {})", statusCode, ResponseHelpers.class, TypeReference.class, resultType);
			} else {
				this.responseParser
					.addStatement("case $L: return $T.parseErrorJson(responseInfo, $T.class)", statusCode, ResponseHelpers.class, resultType);
			}
		}
	}

	@Override
	public void emitDependencies(ClassNameSpace classNameSpace) {
		for (var parameter : route.getParameters()) {
			var parameterType = resolveTypeName(classNameSpace, parameter.getName(), parameter.getSchema());
			parameterTypes.put(parameter, parameterType);
		}
		var requestBody = route.getRequestBody();
		if (requestBody != null) {
			assert requestBody.getContent().size() == 1;
			var content = Objects.requireNonNull(requestBody.getContent().get("application/json"));
			requestBodyType = resolveTypeName(classNameSpace, "RequestBody", content.getSchema());
		}
		for (var entry : route.getResponses().entrySet()) {
			var statusCode = entry.getKey();
			var response = entry.getValue();
			if (response.getContent().isEmpty()) continue;
			var content = response.getContent().get("application/json"); // TODO: handle non-json
			var type = resolveTypeName(
				classNameSpace,
				statusCode != 200 ? "Response" + statusCode : "Response",
				content.getSchema());
			if (statusCode == 200)
				successResponseType = type;
			responseContentTypes.put(content, type);
		}
	}

	public TypeName getSuccessResponseType() {
		return successResponseType;
	}

	@Override
	public void writeType(TypeSpec.Builder spec, ClassNameSpace classNameSpace) {
		spec.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
			.addMethod(MethodSpec.methodBuilder("method")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.returns(Route.Method.class)
				.addStatement("return $T.$L", Route.Method.class, method.toUpperCase(Locale.ROOT))
				.build())
			.addMethod(MethodSpec.methodBuilder("rawPath")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.returns(String.class)
				.addStatement("return $S", path)
				.build());

		addBodyMethod(spec);

		for (var parameter : route.getParameters()) {
			var parameterFieldName = fieldNames.allocateName(parameter.getName());
			var parameterType = parameterTypes.get(parameter);
			spec
				.addField(parameterType, parameterFieldName, Modifier.PRIVATE)
				.addMethod(MethodSpec.methodBuilder(parameterFieldName)
					.returns(parameterType)
					.addModifiers(Modifier.PUBLIC)
					.addStatement("return this.$L", parameterFieldName)
					.build());
			parameterNameToField.put(parameter.getName(), parameterFieldName);
			allParametersConstructor
				.addParameter(parameterType, parameterFieldName)
				.addStatement("this.$L = $L", parameterFieldName, parameterFieldName);
		}

		addPathMethod(spec);


		this.responseParser = MethodSpec.methodBuilder("handleResponseInfo")
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(TypeUtils.buildSuppressWarnings(Stream.of("RedundantSuppression", "SwitchStatementWithTooFewBranches", "EnhancedSwitchMigration", "DuplicateBranchesInSwitch")))
			.addAnnotation(Override.class)
			.addParameter(HttpResponse.ResponseInfo.class, "responseInfo")
			.beginControlFlow("switch (responseInfo.statusCode())");

		for (var entry : route.getResponses().entrySet()) {
			var statusCode = entry.getKey();
			var response = entry.getValue();
			generateErrorResponse(spec, statusCode, response);
		}

		spec.addMethod(allParametersConstructor.build());
		spec.addMethod(this.responseParser
			.addStatement("default: return $T.unknownResponseError(responseInfo)", ResponseHelpers.class)
			.endControlFlow()
			.build());

	}
}
