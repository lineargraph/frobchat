package moe.nea.frobjson.generator;

import com.palantir.javapoet.*;
import moe.nea.frobjson.internal.JsonUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public record SchemaArrayType(
	SchemaType items
) implements SchemaType {
	@Override
	public String name() {
		return items.name() + "[]";
	}

	@Override
	public TypeName typeName() {
		return ParameterizedTypeName.get(ClassName.get(List.class), items.typeName());
	}

	@Override
	public List<? extends JavaFile> emitFiles() {
		return List.of();
	}

	@Override
	public CodeBlock accessDeserialize(String jsonVariable) {
		var innerVariable = jsonVariable + "$inner";
		return CodeBlock.of("$T.stream($L.getAsJsonArray()).map($L -> $L).toList()",
			JsonUtil.class,
			jsonVariable,
			innerVariable,
			items.accessDeserialize(innerVariable)
		);
	}

	@Override
	public CodeBlock accessSerialize(String sourceVariable) {
		var innerVariable = sourceVariable + "$inner";
		return CodeBlock.of("$T.unstream($L.stream().map($L -> $L))",
			JsonUtil.class,
			sourceVariable,
			innerVariable,
			items.accessSerialize(innerVariable)
		);
	}
}
