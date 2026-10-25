package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import moe.nea.frobjson.generator.NameCollection;
import moe.nea.frobmatrix.MxUserId;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.util.List;

public interface ClassGenerator {

	void emitDependencies(ClassNameSpace classNameSpace);

	void writeType(TypeSpec.Builder spec, ClassNameSpace classNameSpace);

	default String guessFieldName(String name, MemberType memberType) {
		if (name.startsWith("m.")) // TODO: this is matrix specific, add more generic interface
			name = name.substring(2);
		return name;
	}

	default TypeName resolveObjectType(
		NameSpace context,
		String nameHint,
		RawJsonType.ObjectType objectType
	) {
		var title = objectType.getTitle();
		var cns = context.createChild(NameCollection.foldName(title != null ? title : nameHint, NameCollection.NameType.CLASS_NAME));
		cns.setGenerator(new ObjectTypeGenerator(
			objectType
		));
		return cns.getClassName();
	}

	default TypeName resolveTypeName(NameSpace context, String nameHint, RawJsonType jsonType) {
		switch (jsonType) {
			case RawJsonType.ObjectType objectType -> {
				return resolveObjectType(context, nameHint, objectType);
			}
			case RawJsonType.StringType stringType -> {
				return switch (stringType.getFormat()) {
					case "mx-user-id" -> ClassName.get(MxUserId.class);
					case "mx-server-name" -> ClassName.get(String.class); // TODO: custom server name class
					case "uri" -> ClassName.get(URI.class);
					case null -> ClassName.get(String.class);
					default -> throw new IllegalStateException("Unexpected format: " + stringType.getFormat());
				};
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
				return ParameterizedTypeName.get(ClassName.get(List.class), resolveTypeName(context, nameHint, arrayType.getItems()));
			}
		}
	}

	default TypeName resolveTypeName(NameSpace context, String nameHint, MemberType memberType) {
		switch (memberType) {
			case MemberType.DirectDeclaration directDeclaration -> {
				var type = directDeclaration.type();
				return resolveTypeName(context, nameHint, type);
			}
			case MemberType.Reference reference -> {
				return reference.schemaReference().className();
			}
			case MemberType.ExtendsDefinition extendsDefinition -> {
				return resolveExtendsType(context, nameHint, extendsDefinition);
			}
		}
	}

	default TypeName resolveExtendsType(
		NameSpace context,
		String nameHint,
		MemberType.ExtendsDefinition extendsDefinition
	) {
		var title = extendsDefinition.guessTitle();
		var cns = context.createChild(NameCollection.foldName(title != null ? title : nameHint, NameCollection.NameType.CLASS_NAME));
		cns.setGenerator(new ExtendsTypeGenerator(extendsDefinition.getParent(), extendsDefinition.getOverrides()));
		return cns.getClassName();
	}
}
