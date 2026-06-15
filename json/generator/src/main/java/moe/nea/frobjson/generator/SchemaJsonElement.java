package moe.nea.frobjson.generator;

import com.google.gson.JsonElement;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeName;

import java.util.List;

public class SchemaJsonElement implements SchemaType {
	@Override
	public String name() {
		return "empty";
	}

	@Override
	public TypeName typeName() {
		return ClassName.get(JsonElement.class);
	}

	@Override
	public List<? extends JavaFile> emitFiles() {
		return List.of();
	}

	@Override
	public CodeBlock accessDeserialize(String jsonVariable) {
		return CodeBlock.of("$L", jsonVariable);
	}

	@Override
	public CodeBlock accessSerialize(String sourceVariable) {
		return CodeBlock.of("$L", sourceVariable);
	}
}
