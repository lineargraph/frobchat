package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.*;
import moe.nea.frobjson.generator.TypeUtils;
import moe.nea.frobmatrix.api.ResponseHelpers;
import moe.nea.frobmatrix.api.Route;

import javax.lang.model.element.Modifier;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RouteGenerator extends JavaFileGenerator {
	private final ApiGenerator generator;
	private final String path;
	private final String method;
	final RawOpenApiRoutes.Route route;
	MethodSpec.Builder responseParser;
	Map<String, String> parameterNameToField = new HashMap<>();

	public RouteGenerator(
		ApiGenerator generator, String path, String method,
		RawOpenApiRoutes.Route route,
		ClassName className) {
		super(generator.schemaLibrary(), className);
		this.generator = generator;
		this.path = path;
		this.method = method;
		this.route = route;
		this.topLevelSpec.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
	}

	public static final Pattern PATH_PATTERN = Pattern.compile("\\{([^}]+)\\}");

	public JavaFile generate() {
		topLevelSpec.addMethod(MethodSpec.methodBuilder("method")
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(Override.class)
			.returns(Route.Method.class)
			.addStatement("return $T.$L", Route.Method.class, method.toUpperCase(Locale.ROOT))
			.build());
		topLevelSpec.addMethod(MethodSpec.methodBuilder("rawPath")
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(Override.class)
			.returns(String.class)
			.addStatement("return $S", path)
			.build());

		var allParameters = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC);

		for (var parameter : route.getParameters()) {
			var parameterFieldName = fieldNames.allocateName(parameter.getName());
			var parameterType = generateAdHocTypeDefinition(parameter.getName(), parameter.getSchema());
			topLevelSpec
				.addField(parameterType, parameterFieldName, Modifier.PRIVATE)
				.addMethod(MethodSpec.methodBuilder(parameterFieldName)
					.returns(parameterType)
					.addModifiers(Modifier.PUBLIC)
					.addStatement("return this.$L", parameterFieldName)
					.build());
			parameterNameToField.put(parameter.getName(), parameterFieldName);
			allParameters
				.addParameter(parameterType, parameterFieldName)
				.addStatement("this.$L = $L", parameterFieldName, parameterFieldName);
		}

		topLevelSpec.addMethod(allParameters.build());

		addPathMethod();

		this.responseParser = MethodSpec.methodBuilder("handleResponseInfo")
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(TypeUtils.buildSuppressWarnings(Stream.of("RedundantSuppression", "SwitchStatementWithTooFewBranches", "EnhancedSwitchMigration", "DuplicateBranchesInSwitch")))
			.addAnnotation(Override.class)
			.addParameter(HttpResponse.ResponseInfo.class, "responseInfo")
			.beginControlFlow("switch (responseInfo.statusCode())");

		for (var entry : route.getResponses().entrySet()) {
			var statusCode = entry.getKey();
			var response = entry.getValue();
			generateErrorResponse(statusCode, response);
		}

		topLevelSpec.addMethod(this.responseParser
			.addStatement("default: return $T.unknownResponseError(responseInfo)", ResponseHelpers.class)
			.endControlFlow()
			.build());
		return JavaFile.builder(topLevelClass.packageName(), topLevelSpec.build()).build();
	}

	private void addPathMethod() {
		var pathMatcher = PATH_PATTERN.matcher(path);
		var method = MethodSpec.methodBuilder("path")
			.addModifiers(Modifier.PUBLIC)
			.returns(String.class)
			.addAnnotation(Override.class);
		method.addCode("return ");
		int lastAppendPosition = 0;
		boolean isFirst = true;
		while (pathMatcher.find()) {
			if (isFirst) {
				isFirst = false;
			} else {
				method.addCode(" + ");
			}
			var skipped = path.substring(lastAppendPosition, pathMatcher.start());
			if (!skipped.isEmpty())
				method.addCode("$S + ", skipped);
			method.addCode("$T.urlEncodeString($L)", ResponseHelpers.class, Objects.requireNonNull(parameterNameToField.get(pathMatcher.group(1))));
			lastAppendPosition = pathMatcher.end();
		}
		var rest = path.substring(lastAppendPosition);
		if (!rest.isEmpty()) {
			if (!isFirst)
				method.addCode(" + ");
			method.addCode("$S", rest);
		}
		method.addCode(";");
		topLevelSpec.addMethod(method.build());
	}


	private void generateErrorResponse(int statusCode, RawOpenApiRoutes.Response response) {
		for (var entry : response.getContent().entrySet()) {
			var contentType = entry.getKey();
			if (!contentType.equals("application/json"))
				throw new RuntimeException("Unhandled content type application/json");
			var responseContent = entry.getValue();
			boolean isError = statusCode != 200;
			TypeName resultType = generateAdHocTypeDefinition(
				isError ? "Response" + statusCode : "Response",
				responseContent.getSchema());
			if (!isError) {
				this.topLevelSpec.superclass(ParameterizedTypeName.get(ClassName.get(Route.class), resultType));
				this.responseParser
					.returns(ParameterizedTypeName.get(ClassName.get(HttpResponse.BodySubscriber.class), resultType))
					.addStatement("case $L: return $T.parseJson(responseInfo, $T.class)", statusCode, ResponseHelpers.class, resultType);
			} else {
				this.responseParser
					.addStatement("case $L: return $T.parseErrorJson(responseInfo, $T.class)", statusCode, ResponseHelpers.class, resultType);
			}
		}
	}
}
