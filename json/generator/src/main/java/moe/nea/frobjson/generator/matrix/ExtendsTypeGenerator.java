package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExtendsTypeGenerator extends ObjectTypeGenerator {
	private final SchemaReference parent;
	private final RawJsonType.ObjectType overrides;
	private final ObjectTypeGenerator parentGenerator;
	private final List<Property> allProperties;

	public ExtendsTypeGenerator(SchemaReference parent, RawJsonType.ObjectType overrides) {
		super(overrides);
		this.parent = parent;
		this.overrides = overrides;
		this.parentGenerator = Objects.requireNonNull((ObjectTypeGenerator) parent.classNameSpace().getGenerator());
		this.allProperties = new ArrayList<>();
		for (var parentProperty : parentGenerator.getAllProperties()) {
			this.allProperties.add(parentProperty);
			// Very efficient removal, yesyes
			getOwnedProperties().removeIf(it -> it.getName().equals(parentProperty.getName()));
		}
		this.allProperties.addAll(getOwnedProperties());
	}

	@Override
	public List<Property> getAllProperties() {
		return allProperties;
	}

	@Override
	public void emitDependencies(ClassNameSpace classNameSpace) {
		super.emitDependencies(classNameSpace);
	}

	@Override
	public void writeType(TypeSpec.Builder spec, ClassNameSpace classNameSpace) {
		spec.superclass(parent.className());
		super.writeType(spec, classNameSpace);
	}
}
