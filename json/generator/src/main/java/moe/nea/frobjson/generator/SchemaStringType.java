package moe.nea.frobjson.generator;

import com.google.gson.JsonPrimitive;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeName;

import java.util.List;

public class SchemaStringType implements SchemaType {
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
	public CodeBlock accessDeserialize(String jsonVariable) {
		return CodeBlock.of("$L.getAsString()", jsonVariable);
	}

	@Override
	public CodeBlock accessSerialize(String sourceVariable) {
		return CodeBlock.of("new $T($L)", JsonPrimitive.class, sourceVariable);
	}
}
