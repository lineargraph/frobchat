package moe.nea.frobjson.generator.matrix;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.javapoet.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import moe.nea.frobjson.generator.NameCollection;
import moe.nea.frobjson.generator.TypeUtils;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectTypeGenerator implements ClassGenerator {
	@Getter
	private final RawJsonType.ObjectType type;
	@Getter
	private final List<Property> ownedProperties;
	private TypeName renderedAdditionalProperties;

	public ObjectTypeGenerator(RawJsonType.ObjectType type) {
		this.type = type;
		ownedProperties = type.getProperties().entrySet().stream()
			.map(entry ->
				new Property(
					entry.getKey(),
					NameCollection.foldName(guessFieldName(entry.getKey(), entry.getValue()), NameCollection.NameType.FIELD_NAME),
					entry.getValue(),
					type.getRequired().contains(entry.getKey()),
					this)
			)
			.collect(Collectors.toCollection(ArrayList::new));
	}

	@Getter
	@RequiredArgsConstructor
	public static class Property {
		final String name;
		final String fieldName;
		final MemberType memberType;
		final boolean required;
		final ObjectTypeGenerator generator;
		@Setter
		TypeName renderedTypeName;
	}

	public List<Property> getAllProperties() {
		return getOwnedProperties();
	}

	@Override
	public void emitDependencies(ClassNameSpace classNameSpace) {
		for (var property : getOwnedProperties()) {
			property.setRenderedTypeName(
				resolveTypeName(classNameSpace, property.getName(), property.getMemberType())
			);
		}
		var additionalProperties = type.getAdditionalProperties();
		if (additionalProperties != null)
			this.renderedAdditionalProperties = resolveTypeName(classNameSpace, null, additionalProperties);
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
		typeSpec.addMethod(MethodSpec.methodBuilder(NameCollection.foldName("set " + fieldName, NameCollection.NameType.METHOD_NAME))
			.addParameter(fieldType, fieldName)
			.addStatement("this.$L = $L", fieldName, fieldName)
			.build());
	}

	public void writePropertyFields(TypeSpec.Builder typeSpec) {
		for (var property : getOwnedProperties()) {
			writePropertyField(property, typeSpec);
		}
		if (renderedAdditionalProperties != null) {
			writeAdditionalProperties(renderedAdditionalProperties, typeSpec);
		}
	}

	public void writeAdditionalProperties(
		TypeName additionalProperties,
		TypeSpec.Builder typeSpec) {
		var mapType = TypeUtils.strMap(additionalProperties);
		typeSpec
			.addField(FieldSpec.builder(mapType, "additionalProperties", Modifier.PRIVATE, Modifier.FINAL)
				.initializer("new $T<>()", LinkedHashMap.class)
				.build())
			.addMethod(MethodSpec.methodBuilder("additionalProperties")
				.addAnnotation(JsonAnyGetter.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(mapType)
				.addStatement("return this.additionalProperties")
				.build())
			.addMethod(MethodSpec.methodBuilder("setAdditionalProperty")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(JsonAnySetter.class)
				.addParameter(String.class, "name")
				.addParameter(additionalProperties, "value")
				.addStatement("this.additionalProperties.put(name, value)")
				.build());
	}

	public void writeToString(TypeSpec.Builder typeSpec, ClassNameSpace classNameSpace) {
		typeSpec.addMethod(MethodSpec.methodBuilder("toString")
			.addAnnotation(Override.class)
			.returns(String.class)
			.addModifiers(Modifier.PUBLIC)
			.addStatement("return $L", CodeBlock.builder()
				.add("$S", String.join(".", classNameSpace.getClassName().simpleNames()) + " [ ")
				.add(getAllProperties()
					.stream()
					.map(it -> CodeBlock.of(" + $S + this.$L()", it.getName() + " = ", it.getFieldName()))
					.collect(CodeBlock.joining(" + \", \"")))
				.add(" + $S", " ]")
				.build())
			.build());
	}


	@Override
	public void writeType(TypeSpec.Builder typeSpec, ClassNameSpace classNameSpace) {
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
		List<CodeBlock> superArgs = new ArrayList<>();
		for (var prop : properties) {
			if (prop.getGenerator() == this) continue;
			constructor.addParameter(prop.getRenderedTypeName(), prop.getFieldName());
			superArgs.add(CodeBlock.of("$L", prop.getFieldName()));
		}
		if (!superArgs.isEmpty())
			constructor.addStatement("super($L)", CodeBlock.join(superArgs, ", "));
		for (var prop : properties) {
			if (prop.getGenerator() != this) continue;
			constructor
				.addParameter(prop.getRenderedTypeName(), prop.getFieldName())
				.addStatement("this.$L = $L", prop.getFieldName(), prop.getFieldName());
		}
		typeSpec.addMethod(constructor.build());
	}

	public void writeConstructors(TypeSpec.Builder typeSpec) {
		var requiredProperties = getAllProperties().stream().filter(Property::isRequired).toList();
		if (!requiredProperties.isEmpty() && requiredProperties.size() != getAllProperties().size()) {
			writeConstructor(typeSpec, requiredProperties);
		}
		writeConstructor(typeSpec, List.of());
		if (!getAllProperties().isEmpty())
			writeConstructor(typeSpec, getAllProperties());
	}
}
