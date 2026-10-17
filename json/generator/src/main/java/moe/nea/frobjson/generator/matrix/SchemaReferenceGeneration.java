package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.JavaFile;

public class SchemaReferenceGeneration extends JavaFileGenerator {
	private final SchemaReference reference;

	public SchemaReferenceGeneration(SchemaReference reference) {
		super(reference.getSchemaLibrary(), reference.getClassName());
		this.reference = reference;
	}


	public JavaFile generate() {
		writeTypeDefinition(topLevelSpec, (RawJsonType.ObjectType) reference.getRawJsonType());
		return JavaFile.builder(reference.getClassName().packageName(), topLevelSpec.build())
			.build();
	}
}
