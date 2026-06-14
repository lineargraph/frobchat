package moe.nea.frobjson.generator;

import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeName;

import java.util.List;

public interface SchemaType {
	String name();

	TypeName typeName();

	default void decorateField(FieldSpec.Builder field) {
	}

	List<? extends JavaFile> emitFiles();

	/// _must_ emit code along the lines of `deserialize({jsonVariable});`
	CodeBlock accessDeserialize(String jsonVariable);

	/// _must_ emit code along the lines of `serialize({sourceVariable});`
	CodeBlock accessSerialize(String sourceVariable);
}
