package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;
import moe.nea.frobmatrix.api.Route;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoutesGenerator {
	final ApiGenerator generator;
	final RawOpenApiRoutes routes;
	final String packageName;

	MatrixGenerator matrixGenerator() {
		return generator.matrixGenerator();
	}

	public RoutesGenerator(ApiGenerator generator, RawOpenApiRoutes routes, String packageName) {
		this.generator = generator;
		this.routes = routes;
		this.packageName = packageName;

	}

	public List<JavaFile> generate() {
		List<JavaFile> files = new ArrayList<>();
		for (var pathEntry : routes.getPaths().entrySet()) {
			var path = pathEntry.getKey();
			var methods = pathEntry.getValue();
			for (var routeEntry : methods.entrySet()) {
				var method = routeEntry.getKey();
				var route = routeEntry.getValue();
				files.add(generateRoute(path, method, route));
			}
		}
		return files;
	}

	private JavaFile generateRoute(String path, String method, RawOpenApiRoutes.Route route) {
		var className = matrixGenerator().allocateClassName(packageName, route.getOperationId());
		var typeSpec = TypeSpec.classBuilder(className)
			.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
			.superclass(Route.class);
		typeSpec.addMethod(MethodSpec.methodBuilder("method")
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(Override.class)
			.returns(Route.Method.class)
			.addStatement("return $T.$L", Route.Method.class, method.toUpperCase(Locale.ROOT))
			.build());
		typeSpec.addMethod(MethodSpec.methodBuilder("rawPath")
			.addModifiers(Modifier.PUBLIC)
			.addAnnotation(Override.class)
			.returns(String.class)
			.addStatement("return $S", path)
			.build());
		return JavaFile.builder(packageName, typeSpec.build()).build();
	}
}
