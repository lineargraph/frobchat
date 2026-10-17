package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import moe.nea.frobjson.generator.NameCollection;

public abstract class JavaFileGenerator {
	final NameCollection subTypes = new NameCollection(NameCollection.NameType.CLASS_NAME, true);
	private final SchemaLibrary schemaLibrary;
	final ClassName topLevelClass;
	final TypeSpec.Builder typeSpec;

	protected JavaFileGenerator(
		SchemaLibrary schemaLibrary,
		ClassName className) {
		this.schemaLibrary = schemaLibrary;
		topLevelClass = className;
		typeSpec = TypeSpec.classBuilder(className);
	}


	public TypeName generateAdHocTypeDefinition(String nameHint, RawJsonType jsonType) {
		var title = jsonType.getTitle();
		var name = title == null ? nameHint : title;
		var reference = new SchemaReference(
			schemaLibrary,
			jsonType,
			topLevelClass.nestedClass(name)
		);
		typeSpec.addType(new SchemaReferenceGeneration(reference).generateNested(subTypes, topLevelClass, typeSpec));
		return reference.getClassName();
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
