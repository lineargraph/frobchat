package moe.nea.frobjson.generator;

import com.palantir.javapoet.*;
import moe.nea.frobjson.internal.JsonUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
	public CodeBlock deserializeLambda(String variableName) {
		return CodeBlock.of(
			"$T.liftArray($L, $T.toList())",
			JsonUtil.class,
			items.deserializeLambda(variableName + "$inner"),
			Collectors.class
		);
	}

	@Override
	public CodeBlock accessDeserialize(String jsonVariable) {
		return CodeBlock.of("$T.stream($L.getAsJsonArray()).map($L).toList()",
			JsonUtil.class,
			jsonVariable,
			items.deserializeLambda(jsonVariable + "$inner")
		);
	}

	@Override
	public CodeBlock serializeLambda(String variableName) {
		return CodeBlock.of(
			"$T.liftUnArray($L)",
			JsonUtil.class,
			items.serializeLambda(variableName + "$inner")
		);
	}

	@Override
	public CodeBlock accessSerialize(String sourceVariable) {
		return CodeBlock.of("$T.unstream($L.stream().map($L))",
			JsonUtil.class,
			sourceVariable,
			items.serializeLambda(sourceVariable + "$inner")
		);
	}
}
