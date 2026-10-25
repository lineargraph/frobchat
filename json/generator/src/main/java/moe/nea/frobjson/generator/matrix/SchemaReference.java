package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.ClassName;

public record SchemaReference(ClassNameSpace classNameSpace) {
	public ClassName className() {
		return classNameSpace.getClassName();
	}
}
