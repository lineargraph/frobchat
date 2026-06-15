package moe.nea.frobjson.generator;

import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class LazySchema implements SchemaType {
	@Nullable SchemaType instance = null;
	Supplier<? extends SchemaType> supp;

	@Override
	public SchemaType unlazy() {
		return get().unlazy();
	}

	public LazySchema(Supplier<? extends SchemaType> supp) {
		this.supp = supp;
	}

	public SchemaType get() {
		if (instance == null)
			return instance = supp.get();
		return instance;
	}

	@Override
	public String toString() {
		return "Lazy[" + get() + "]";
	}

	@Override
	public String name() {
		return get().name();
	}

	@Override
	public TypeName typeName() {
		return get().typeName();
	}

	@Override
	public List<? extends JavaFile> emitFiles() {
		return get().emitFiles();
	}

	@Override
	public CodeBlock accessDeserialize(String jsonVariable) {
		return get().accessDeserialize(jsonVariable);
	}

	@Override
	public CodeBlock accessSerialize(String sourceVariable) {
		return get().accessSerialize(sourceVariable);
	}

	@Override
	public void decorateField(FieldSpec.Builder field) {
		get().decorateField(field);
	}

	@Override
	public CodeBlock deserializeLambda(String variableName) {
		return get().deserializeLambda(variableName);
	}

	@Override
	public CodeBlock serializeLambda(String variableName) {
		return get().serializeLambda(variableName);
	}
}
