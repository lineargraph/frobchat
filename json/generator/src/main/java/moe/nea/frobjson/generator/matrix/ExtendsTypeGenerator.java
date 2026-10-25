package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.TypeSpec;

public class ExtendsTypeGenerator extends ObjectTypeGenerator {
	private final SchemaReference parent;
	private final RawJsonType.ObjectType overrides;

	public ExtendsTypeGenerator(SchemaReference parent, RawJsonType.ObjectType overrides) {
		super(overrides);
		this.parent = parent;
		this.overrides = overrides;
	}

	@Override
	public void emitDependencies(NameSpace.ClassNameSpace classNameSpace) {
		super.emitDependencies(classNameSpace);
	}

	@Override
	public void writeType(TypeSpec.Builder spec, NameSpace.ClassNameSpace classNameSpace) {
		// TODO: this is all entirely wrong, lol
	}
}
