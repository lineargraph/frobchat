package moe.nea.frobjson.generator;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class TypeUtils {
	public static TypeName MAP_STR_STR = ParameterizedTypeName.get(Map.class, String.class, String.class);

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
