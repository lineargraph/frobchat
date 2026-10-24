package moe.nea.frobjson.generator.matrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.javapoet.*;
import moe.nea.frobjson.generator.NameCollection;
import tools.jackson.databind.JsonNode;

import javax.lang.model.element.Modifier;
import java.util.List;

public abstract class JavaFileGenerator {
	final NameCollection subTypes = new NameCollection(NameCollection.NameType.CLASS_NAME, true);
	final NameCollection fieldNames = new NameCollection(NameCollection.NameType.FIELD_NAME, true);
	private final SchemaLibrary schemaLibrary;
	final ClassName topLevelClass;
	final TypeSpec.Builder topLevelSpec;

	protected JavaFileGenerator(
		SchemaLibrary schemaLibrary,
		ClassName className) {
		this.schemaLibrary = schemaLibrary;
		topLevelClass = className;
		subTypes.allocateName(className.simpleName());
		topLevelSpec = TypeSpec.classBuilder(className);
	}

	protected void writeTypeDefinition(
		TypeSpec.Builder typeSpec,
		RawJsonType.ObjectType type
	) {
		typeSpec.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		var nameCollection = new NameCollection(NameCollection.NameType.FIELD_NAME, true);
		var constructor = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC);
		var requiredConstructor = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC);
		var toString = CodeBlock.builder()
			.add("$S", type.getTitle() + " [");
		boolean isFirstProperty = true;
		boolean anyNonRequired = false;
		for (var entry : type.getProperties().entrySet()) {
			var key = entry.getKey();
			var value = entry.getValue();
			var fieldName = nameCollection.allocateName(guessFieldName(key, value));
			var fieldType = getType(fieldName, value);
			var fieldSpec = (FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE));
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


	private TypeName getType(String nameHint, MemberType type) {
		return generateAdHocTypeDefinition(nameHint, type);
	}

	private String guessFieldName(String name, MemberType memberType) {
		if (name.startsWith("m.")) // TODO: this is matrix specific, add more generic interface
			name = name.substring(2);
		return name;
	}

	public TypeName generateAdHocTypeDefinition(String nameHint, RawJsonType jsonType) {
		switch (jsonType) {
			case RawJsonType.ObjectType objectType -> {
				var title = objectType.getTitle();
				var simpleName = subTypes.allocateName(title != null ? title : nameHint);
				var className = topLevelClass.nestedClass(simpleName);
				var typeSpec = TypeSpec.classBuilder(className)
					.addModifiers(Modifier.STATIC);
				writeTypeDefinition(typeSpec, objectType);
				topLevelSpec.addType(typeSpec.build());
				return className;
			}
			case RawJsonType.StringType stringType -> {
				return ClassName.get(String.class);
			}
			case RawJsonType.UnknownType unknownType -> {
				return ClassName.get(JsonNode.class);
			}
			case RawJsonType.BooleanType booleanType -> {
				return TypeName.BOOLEAN;
			}
			case RawJsonType.IntegerType integerType -> {
				if ("int64".equals(integerType.getFormat()))
					return TypeName.LONG;
				return TypeName.INT; // TODO: perhaps long always?
			}
			case RawJsonType.ArrayType arrayType -> {
				// TODO: bind the title and description and such from outside? unsure
				return ParameterizedTypeName.get(ClassName.get(List.class), getType(nameHint, arrayType.getItems()));
			}
		}
	}

	public TypeName generateAdHocTypeDefinition(String nameHint, MemberType memberType) {
		switch (memberType) {
			case MemberType.DirectDeclaration directDeclaration -> {
				var type = directDeclaration.type();
				return generateAdHocTypeDefinition(nameHint, type);
			}
			case MemberType.Reference reference -> {
				return reference.schemaReference().getClassName();
			}
		}
	}
}
