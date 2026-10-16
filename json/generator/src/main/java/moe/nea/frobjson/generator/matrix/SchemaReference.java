package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import lombok.Getter;

@Getter
public class SchemaReference {
	private final SchemaLibrary schemaLibrary;
	private final RawJsonType rawJsonType;
	private final ClassName className;

	public SchemaReference(SchemaLibrary schemaLibrary, RawJsonType rawJsonType, ClassName className) {
		this.schemaLibrary = schemaLibrary;
		this.rawJsonType = rawJsonType;
		this.className = className;
	}

	public JavaFile generateDefinition() {
		return new SchemaReferenceGeneration(this).generate();
	}
}
