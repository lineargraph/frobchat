package moe.nea.frobjson.generator;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
	public static AnnotationSpec buildSuppressWarnings(Stream<? extends String> warnings) {
		return AnnotationSpec.builder(SuppressWarnings.class)
			.addMember("value", warnings
				.map(it -> CodeBlock.of("$S", it))
				.collect(CodeBlock.joining(", ", "{", "}")))
			.build();
	}
	public static Pattern linkPattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]+)\\)");
	public static Pattern codePattern = Pattern.compile("`([^`]+)`");

	public static String formatJavadoc(String description) {
		var sb = new StringBuilder();
		{
			var matcher = linkPattern.matcher(description
					.replace("&", "&amp;")
					.replace("<", "&lt;")
					.replace(">", "&gt;")
					.replace("}", "&rcub;")
					.replace("{", "&lcub;")
				.replace("\n\n", "\n<br>\n"));
			while (matcher.find()) {
				matcher.appendReplacement(sb, "<a href=\"$2\">$1</a>");
			}
			matcher.appendTail(sb);
		}
		{
			var matcher = codePattern.matcher(sb.toString());
			sb.setLength(0);
			while (matcher.find()) {
				matcher.appendReplacement(sb, "{@code $1}");
			}
			matcher.appendTail(sb);
		}
		return sb.toString();
	}
}
