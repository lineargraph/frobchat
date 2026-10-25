package moe.nea.frobjson.generator.matrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.javapoet.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import moe.nea.frobjson.generator.NameCollection;
import moe.nea.frobjson.generator.TypeUtils;

import javax.lang.model.element.Modifier;
import java.util.List;

public class ObjectTypeGenerator implements ClassGenerator {
	@Getter
	private final RawJsonType.ObjectType type;
	@Getter
	private final List<Property> ownedProperties;

	public ObjectTypeGenerator(RawJsonType.ObjectType type) {
		this.type = type;
		ownedProperties = type.getProperties().entrySet().stream()
			.map(entry ->
				new Property(
					entry.getKey(),
					NameCollection.foldName(guessFieldName(entry.getKey(), entry.getValue()), NameCollection.NameType.FIELD_NAME),
					entry.getValue(),
					type.getRequired().contains(entry.getKey()))
			)
			.toList();
	}

	@Getter
	@RequiredArgsConstructor
	public static class Property {
		final String name;
		final String fieldName;
		final MemberType memberType;
		final boolean required;
		@Setter
		TypeName renderedTypeName;
	}

	@Override
	public void emitDependencies(NameSpace.ClassNameSpace classNameSpace) {
		for (var property : getOwnedProperties()) {
			property.setRenderedTypeName(
				resolveTypeName(classNameSpace, property.getName(), property.getMemberType())
			);
		}
	}

	@Override
	public void writeType(TypeSpec.Builder typeSpec, NameSpace.ClassNameSpace classNameSpace) {
		typeSpec.addModifiers(Modifier.PUBLIC);
		if (type.isDeprecated())
			typeSpec.addAnnotation(Deprecated.class);
		var constructor = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC);
		var requiredConstructor = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC);

		boolean anyNonRequired = false;
		for (var property : getOwnedProperties()) {
			var deprecated = property.getMemberType() instanceof MemberType.DirectDeclaration decl
				&& decl.type().isDeprecated();
			var fieldType = property.getRenderedTypeName();
			var fieldName = property.getFieldName();
			var fieldSpec = FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE)
				.addAnnotations(TypeUtils.deprecatedIf(deprecated));
			var jsonPropertySpec = AnnotationSpec.builder(JsonProperty.class);
			if (!fieldName.equals(property.getName())) {
				jsonPropertySpec.addMember("value", "$S", property.getName());
			}
			if (property.isRequired()) {
				jsonPropertySpec.addMember("required", "true");
				requiredConstructor.addParameter(fieldType, fieldName)
					.addStatement("this.$L = $L", fieldName, fieldName);
			} else {
				anyNonRequired = true;
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

		}
		var toString = CodeBlock.builder()
			.add("$S + ", String.join(".", classNameSpace.getClassName().simpleNames()) + " [ ")
			.add(getOwnedProperties()
				.stream()
				.map(it -> CodeBlock.of("$S + $L", it.getName() + " = ", it.getFieldName()))
				.collect(CodeBlock.joining(" + \", \" + ")));

		toString.add(" + $S", " ]");
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
