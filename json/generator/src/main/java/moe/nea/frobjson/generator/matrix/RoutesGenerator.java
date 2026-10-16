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
				try {
					files.add(generateRoute(path, method, route));
				} catch (Exception e) {
					throw new RuntimeException("While generating operation " + route.getOperationId(), e);
				}
			}
		}
		return files;
	}

	private JavaFile generateRoute(String path, String method, RawOpenApiRoutes.Route route) {
		var className = matrixGenerator().allocateClassName(packageName, route.getOperationId());
		return new RouteGenerator(generator, path, method, route, className).generate();
	}
}
