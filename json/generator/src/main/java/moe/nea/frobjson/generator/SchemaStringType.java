package moe.nea.frobjson.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeName;
import moe.nea.frobjson.internal.JsonUtil;

import java.util.List;

public record SchemaStringType() implements SchemaType {
	@Override
	public String name() {
		return "String";
	}

	@Override
	public TypeName typeName() {
		return ClassName.get(String.class);
	}

	@Override
	public List<? extends JavaFile> emitFiles() {
		return List.of();
	}

	@Override
	public CodeBlock deserializeLambda(String variableName) {
		return CodeBlock.of("$T::getAsString", JsonElement.class);
	}

	@Override
	public CodeBlock accessDeserialize(String jsonVariable) {
		return CodeBlock.of("$L.getAsString()", jsonVariable);
	}

	@Override
	public CodeBlock serializeLambda(String variableName) {
		return CodeBlock.of("$T::new", JsonPrimitive.class);
	}

	@Override
	public CodeBlock accessSerialize(String sourceVariable) {
		return CodeBlock.of("new $T($L)", JsonPrimitive.class, sourceVariable);
	}
}
