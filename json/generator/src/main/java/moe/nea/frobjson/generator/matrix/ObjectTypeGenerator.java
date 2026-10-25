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
import java.util.stream.Stream;

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

	public void writePropertyField(Property property, TypeSpec.Builder typeSpec) {
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
		}
		fieldSpec.addAnnotation(jsonPropertySpec.build());
		typeSpec.addField(fieldSpec.build());
		typeSpec.addMethod(MethodSpec.methodBuilder(fieldName)
			.addModifiers(Modifier.PUBLIC)
			.returns(fieldType)
			.addStatement("return this.$L", fieldName)
			.addAnnotations(TypeUtils.deprecatedIf(deprecated))
			.build());
		StringBuilder setterName = new StringBuilder("set");
		NameCollection.capitalize(setterName, fieldName);
		typeSpec.addMethod(MethodSpec.methodBuilder(setterName.toString())
			.addParameter(fieldType, fieldName)
			.addStatement("this.$L = $L", fieldName, fieldName)
			.build());
	}

	public void writePropertyFields(TypeSpec.Builder typeSpec) {
		for (var property : getOwnedProperties()) {
			writePropertyField(property, typeSpec);
		}
	}

	public void writeToString(TypeSpec.Builder typeSpec, NameSpace.ClassNameSpace classNameSpace) {
		typeSpec.addMethod(MethodSpec.methodBuilder("toString")
			.addAnnotation(Override.class)
			.returns(String.class)
			.addModifiers(Modifier.PUBLIC)
			.addStatement("return $L", CodeBlock.builder()
				.add("$S", String.join(".", classNameSpace.getClassName().simpleNames()) + " [ ")
				.add(getOwnedProperties()
					.stream()
					.map(it -> CodeBlock.of(" + $S + $L", it.getName() + " = ", it.getFieldName()))
					.collect(CodeBlock.joining(" + \", \"")))
				.add(" + $S", " ]")
				.build())
			.build());
	}


	@Override
	public void writeType(TypeSpec.Builder typeSpec, NameSpace.ClassNameSpace classNameSpace) {
		typeSpec.addModifiers(Modifier.PUBLIC);
		if (type.isDeprecated())
			typeSpec.addAnnotation(Deprecated.class);
		writePropertyFields(typeSpec);
		writeToString(typeSpec, classNameSpace);
		writeConstructors(typeSpec);
	}

	public void writeConstructor(TypeSpec.Builder typeSpec, List<Property> properties) {
		var constructor = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC);
		for (var prop : properties) {
			constructor
				.addParameter(prop.getRenderedTypeName(), prop.getFieldName())
				.addStatement("this.$L = $L", prop.getFieldName(), prop.getFieldName());
		}
		typeSpec.addMethod(constructor.build());
	}

	public void writeConstructors(TypeSpec.Builder typeSpec) {
		var requiredProperties = getOwnedProperties().stream().filter(Property::isRequired).toList();
		if (!requiredProperties.isEmpty() && requiredProperties.size() != getOwnedProperties().size()) {
			writeConstructor(typeSpec, requiredProperties);
		}
		writeConstructor(typeSpec, List.of());
		if (!getOwnedProperties().isEmpty())
			writeConstructor(typeSpec, getOwnedProperties());
	}
}
