package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;
import moe.nea.frobjson.generator.NameCollection;
import moe.nea.frobmatrix.api.MatrixRESTClient;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RouteCategoryGenerator implements ClassGenerator {
	public List<ClassNameSpace> routes = new ArrayList<>();

	@Override
	public void emitDependencies(ClassNameSpace classNameSpace) {
		routes.sort(Comparator.comparing(ClassNameSpace::getClassName));
	}

	@Override
	public void writeType(TypeSpec.Builder spec, ClassNameSpace classNameSpace) {
		spec.addModifiers(Modifier.PUBLIC)
			.addField(MatrixRESTClient.class, "restClient", Modifier.FINAL)
			.addMethod(MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(MatrixRESTClient.class, "restClient")
				.addStatement("this.restClient = restClient")
				.build());
		for (var rns : routes) {
			var routeGenerator = (RoutesGenerator) rns.getGenerator();
			spec.addMethod(MethodSpec.methodBuilder(NameCollection.foldMethod(rns.getClassName().simpleName()))
				.addModifiers(Modifier.PUBLIC)
				.returns(rns.getClassName())
				.addStatement("return new $T(this.restClient)", rns.getClassName())
				.build());
		}
	}
}
