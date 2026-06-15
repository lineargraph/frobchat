package moe.nea.frobjson.generator;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

public class TypeUtils {
	public static TypeName required(
		boolean required,
		TypeName name
	) {
		if (!required) {
			return name.annotated(AnnotationSpec.builder(Nullable.class).build());
		}
		return name;
	}

}
