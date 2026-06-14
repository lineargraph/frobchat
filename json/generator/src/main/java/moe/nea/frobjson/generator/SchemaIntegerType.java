package moe.nea.frobjson.generator;

import com.google.gson.JsonPrimitive;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeName;

import java.util.List;

public class SchemaIntegerType implements SchemaType {
	@Override
	public String name() {
		return "Integer";
	}

	@Override
	public TypeName typeName() {
		return ClassName.get(Integer.class);
	}

	@Override
	public List<? extends JavaFile> emitFiles() {
		return List.of();
	}

	@Override
	public CodeBlock accessDeserialize(String destinationVariable, String jsonVariable) {
		return CodeBlock.of("$L = $L.getAsInt();\n", destinationVariable, jsonVariable);
	}

	@Override
	public CodeBlock accessSerialize(String sourceVariable, String jsonVariable) {
		return CodeBlock.of("$L = new $T($L);\n", jsonVariable, JsonPrimitive.class, sourceVariable);
	}
}
