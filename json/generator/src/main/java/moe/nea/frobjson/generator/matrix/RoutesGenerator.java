package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.TypeSpec;
import moe.nea.frobjson.generator.NameCollection;

public class RoutesGenerator implements ClassGenerator {
	final ApiGenerator generator;
	final RawOpenApiRoutes routes;
	final String packageName;

	public RoutesGenerator(ApiGenerator generator, RawOpenApiRoutes routes, String packageName) {
		this.generator = generator;
		this.routes = routes;
		this.packageName = packageName;
	}

	@Override
	public void emitDependencies(NameSpace.ClassNameSpace classNameSpace) {
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
			}
		}
	}

	@Override
	public void writeType(TypeSpec.Builder builder, NameSpace.ClassNameSpace classNameSpace) {
		builder.addJavadoc("TODO: actually do something useful in here");
	}
}
