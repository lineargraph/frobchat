package moe.nea.frobjson.generator.matrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.javapoet.*;
import lombok.Getter;
import moe.nea.frobjson.generator.NameCollection;
import moe.nea.frobjson.generator.TypeUtils;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ObjectTypeGenerator implements ClassGenerator {
	@Getter
	private final RawJsonType.ObjectType type;

	// Type resolution data
	private final Map<String, TypeName> propertyTypes = new HashMap<>();

	public ObjectTypeGenerator(RawJsonType.ObjectType type) {
		this.type = type;
	}


	@Override
	public void emitDependencies(NameSpace.ClassNameSpace classNameSpace) {
		for (var entry : type.getProperties().entrySet()) {
			var name = entry.getKey();
			var memberType = entry.getValue();
			propertyTypes.put(name, resolveTypeName(classNameSpace, name, memberType));
		}
	}

	@Override
	public void writeType(TypeSpec.Builder typeSpec, NameSpace.ClassNameSpace classNameSpace) {
		typeSpec.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		if (type.isDeprecated())
			typeSpec.addAnnotation(Deprecated.class);
		var nameCollection = new NameCollection(NameCollection.NameType.FIELD_NAME, true);
		var constructor = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC);
		var requiredConstructor = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC);
		var toString = CodeBlock.builder()
			.add("$S", String.join(".", classNameSpace.getClassName().simpleNames()) + " [");
		boolean isFirstProperty = true;
		boolean anyNonRequired = false;
		for (var entry : type.getProperties().entrySet()) {
			var key = entry.getKey();
			var value = entry.getValue();
			var fieldName = nameCollection.allocateName(guessFieldName(key, value));
			var deprecated = value instanceof MemberType.DirectDeclaration decl && decl.type().isDeprecated();
			var fieldType = propertyTypes.get(key);
			var fieldSpec = (FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE))
				.addAnnotations(TypeUtils.deprecatedIf(deprecated));
			var jsonPropertySpec = AnnotationSpec.builder(JsonProperty.class);
			if (!fieldName.equals(key)) {
				jsonPropertySpec.addMember("value", "$S", key);
			}
			if (type.getRequired().contains(key)) {
				jsonPropertySpec.addMember("required", "true");
				requiredConstructor.addParameter(fieldType, fieldName)
					.addStatement("this.$L = $L", fieldName, fieldName);
			} else {
				anyNonRequired = true;
			}
			if (isFirstProperty) {
				toString.add(" + $S + this.$L", " " + key + " = ", fieldName);
			} else {
				toString.add(" + $S + this.$L", ", " + key + " = ", fieldName);
			}
			constructor.addParameter(fieldType, fieldName)
				.addStatement("this.$L = $L", fieldName, fieldName);
			fieldSpec.addAnnotation(jsonPropertySpec.build());
			typeSpec.addField(fieldSpec.build());
			typeSpec.addMethod(MethodSpec.methodBuilder(fieldName)
				.addModifiers(Modifier.PUBLIC)
				.returns(fieldType)
				.addStatement("return this.$L", fieldName)
				.addAnnotations(TypeUtils.deprecatedIf(deprecated))
				.build());
			isFirstProperty = false;
		}
		toString.add("+ $S", " ]");
		if (anyNonRequired)
			typeSpec.addMethod(requiredConstructor.build());
		if (!requiredConstructor.build().parameters().isEmpty()) {
			typeSpec.addMethod(MethodSpec.constructorBuilder().build());
		}
		typeSpec.addMethod(constructor.build())
			.addMethod(MethodSpec.methodBuilder("toString")
				.addAnnotation(Override.class)
				.returns(String.class)
				.addModifiers(Modifier.PUBLIC)
				.addStatement("return $L", toString.build())
				.build());
	}
}
