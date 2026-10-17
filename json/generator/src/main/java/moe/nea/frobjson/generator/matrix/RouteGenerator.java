package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.*;
import moe.nea.frobjson.generator.TypeUtils;
import moe.nea.frobmatrix.api.ResponseHelpers;
import moe.nea.frobmatrix.api.Route;

import javax.lang.model.element.Modifier;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.stream.Stream;

public class RouteGenerator extends JavaFileGenerator {
	private final ApiGenerator generator;
	private final String path;
	private final String method;
	final RawOpenApiRoutes.Route route;
	MethodSpec.Builder responseParser;

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
