package moe.nea.frobjson.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeName;

import java.util.List;

public class SchemaBooleanType implements SchemaType {
	@Override
	public String name() {
		return "boolean";
	}

	@Override
	public TypeName typeName() {
		return ClassName.get(Boolean.class);
	}

	@Override
	public List<? extends JavaFile> emitFiles() {
		return List.of();
	}

	@Override
	public CodeBlock serializeLambda(String variableName) {
		return CodeBlock.of("$T::new", JsonPrimitive.class);
	}

	@Override
	public CodeBlock deserializeLambda(String variableName) {
		return CodeBlock.of("$T::getAsBoolean", JsonElement.class);
	}

	@Override
	public CodeBlock accessDeserialize(String jsonVariable) {
		return CodeBlock.of("$L.getAsBoolean()", jsonVariable);
	}

	@Override
	public CodeBlock accessSerialize(String sourceVariable) {
		return CodeBlock.of("new $T($L)", JsonPrimitive.class, sourceVariable);
	}
}
