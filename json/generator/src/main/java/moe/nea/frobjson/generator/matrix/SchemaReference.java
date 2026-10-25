package moe.nea.frobjson.generator.matrix;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.TypeName;

public record SchemaReference(MatrixGenerator matrixGenerator, TypeName typeName) {
	public SchemaReference(ClassNameSpace cns) {
		this(cns.getMatrixGenerator(), cns.getClassName());
	}

	public ClassNameSpace tryGetClassNameSpace() {
		if (typeName instanceof ClassName className)
			return matrixGenerator.getClassNameSpace(className);
		return null;
	}
}
