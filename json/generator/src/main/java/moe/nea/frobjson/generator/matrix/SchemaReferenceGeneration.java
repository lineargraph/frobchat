package moe.nea.frobjson.generator.matrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.javapoet.*;
import moe.nea.frobjson.generator.NameCollection;
import tools.jackson.databind.JsonNode;

import javax.lang.model.element.Modifier;
import java.util.Objects;

public class SchemaReferenceGeneration {
	private final NameCollection subTypes = new NameCollection(NameCollection.NameType.CLASS_NAME, true);
	private final SchemaReference reference;
	TypeSpec.Builder topLevelBuilder;

	public SchemaReferenceGeneration(SchemaReference reference) {
		this.reference = reference;
		subTypes.allocateName(reference.getClassName().simpleName());
		topLevelBuilder = TypeSpec.classBuilder(reference.getClassName());
	}


	public JavaFile generate() {
		generateTypeDefinition(topLevelBuilder, (RawJsonType.ObjectType) reference.getRawJsonType());
		return JavaFile.builder(reference.getClassName().packageName(), topLevelBuilder.build())
			.build();
	}


	private TypeName getType(MemberType type) {
		switch (type) {
			case MemberType.InlineDefinition(RawJsonType innerType) -> {
				switch (innerType) {
					case RawJsonType.ObjectType objectType -> {
						var simpleName = subTypes.allocateName(Objects.requireNonNull(objectType.getTitle()));
						var className = reference.getClassName().nestedClass(simpleName);
						var typeSpec = TypeSpec.classBuilder(className)
							.addModifiers(Modifier.STATIC);
						generateTypeDefinition(typeSpec, objectType);
						topLevelBuilder.addType(typeSpec.build());
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
						return TypeName.INT; // TODO: perhaps long?
					}
				}
			}
			case MemberType.Reference reference -> {
				var schemaRef = reference.schemaReference();
				return schemaRef.getClassName();
			}
			case MemberType.MergeDefinition mergeDefinition -> {
				return getType(new MemberType.InlineDefinition(mergeDefinition.type()));
			}
		}
	}

	private String guessFieldName(String name, MemberType memberType) {
		if (name.startsWith("m."))
			name = name.substring(2);
		return name;
	}

	private void generateTypeDefinition(TypeSpec.Builder typeSpec, RawJsonType.ObjectType type) {
		typeSpec.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		var nameCollection = new NameCollection(NameCollection.NameType.FIELD_NAME, true);
		for (var entry : type.getProperties().entrySet()) {
			var key = entry.getKey();
			var value = entry.getValue();
			var fieldType = getType(value);
			var fieldName = nameCollection.allocateName(guessFieldName(key, value));
			var fieldSpec = (FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE));
			var jsonPropertySpec = AnnotationSpec.builder(JsonProperty.class);
			if (!fieldName.equals(key)) {
				jsonPropertySpec.addMember("value", "$S", key);
			}
			if (type.getRequired().contains(key)) {
				jsonPropertySpec.addMember("required", "true");
			}
			fieldSpec.addAnnotation(jsonPropertySpec.build());
			typeSpec.addField(fieldSpec.build());
			typeSpec.addMethod(MethodSpec.methodBuilder(fieldName)
				.addModifiers(Modifier.PUBLIC)
				.returns(fieldType)
				.addStatement("return this.$L", fieldName)
				.build());
		}
	}

}
