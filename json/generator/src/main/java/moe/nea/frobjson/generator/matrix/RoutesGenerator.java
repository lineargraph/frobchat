package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.*;
import moe.nea.frobjson.generator.NameCollection;
import moe.nea.frobmatrix.api.MatrixRESTClient;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class RoutesGenerator implements ClassGenerator {
	final RawOpenApiRoutes routes;
	final String packageName;

	public RoutesGenerator(RawOpenApiRoutes routes, String packageName) {
		this.routes = routes;
		this.packageName = packageName;
	}

	List<ClassNameSpace> generatedRoutes = new ArrayList<>();

	@Override
	public void emitDependencies(ClassNameSpace classNameSpace) {
		var containingPackage = classNameSpace.getPackageNameSpace();
		var basePath = routes.guessBasePath();
		for (var pathEntry : routes.getPaths().entrySet()) {
			var path = pathEntry.getKey();
			var methods = pathEntry.getValue();
			for (var routeEntry : methods.entrySet()) {
				var method = routeEntry.getKey();
				var route = routeEntry.getValue();
				var routeClassName = NameCollection.foldName(route.getOperationId(), NameCollection.NameType.CLASS_NAME);
				var routeClass = containingPackage.createChild(routeClassName);
				routeClass.setGenerator(new RouteGenerator(basePath + path, method, route));
				generatedRoutes.add(routeClass);
			}
		}
	}

	@Override
	public void writeType(TypeSpec.Builder builder, ClassNameSpace classNameSpace) {
		builder.addModifiers(Modifier.PUBLIC)
			.addField(MatrixRESTClient.class, "restClient")
			.addMethod(MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(MatrixRESTClient.class, "restClient")
				.addStatement("this.restClient = restClient")
				.build());
		for (var rns : generatedRoutes) {
			var route = (RouteGenerator) rns.getGenerator();
			builder.addMethod(MethodSpec.methodBuilder(NameCollection.foldMethod(route.route.getOperationId()))
				.returns(route.getSuccessResponseType())
				.addParameters(route.getAllRequestArguments()
					.stream()
					.map(it -> ParameterSpec.builder(it.right(), NameCollection.foldField(it.left())).build())
					.toList())
				.addStatement("return restClient.execute(new $T($L))",
					rns.getClassName(),
					route.getAllRequestArguments()
						.stream()
						.map(it -> CodeBlock.of("$L", NameCollection.foldField(it.left())))
						.collect(CodeBlock.joining(", "))
				)
				.addModifiers(Modifier.PUBLIC)
				.build());
		}
	}
}
