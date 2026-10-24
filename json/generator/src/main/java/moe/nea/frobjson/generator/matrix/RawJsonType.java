package moe.nea.frobjson.generator.matrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.*;

@Getter
@Setter
@ToString
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = RawJsonType.StringType.class, name = "string"),
	@JsonSubTypes.Type(value = RawJsonType.BooleanType.class, name = "boolean"),
	@JsonSubTypes.Type(value = RawJsonType.ObjectType.class, name = "object"),
	@JsonSubTypes.Type(value = RawJsonType.IntegerType.class, name = "integer"),
	@JsonSubTypes.Type(value = RawJsonType.ArrayType.class, name = "array"),
	@JsonSubTypes.Type(value = RawJsonType.UnknownType.class, name = "unknown")
})
@NoArgsConstructor
@AllArgsConstructor
public sealed abstract class RawJsonType {
	private @Nullable String title;
	private @Nullable String description;
	private @Nullable JsonNode example;
	@JsonProperty("x-addedInMatrixVersion")
	private @Nullable Void addedInMatrixVersion;
	private boolean deprecated = false;

	// Pseudo type for "missing" types. Most likely this is for free form types
	@ToString(callSuper = true)
	@Getter
	public static final class UnknownType extends RawJsonType {
	}

	@ToString(callSuper = true)
	@Getter
	public static final class ArrayType extends RawJsonType {
		private MemberType items;
	}

	@ToString(callSuper = true)
	@Getter
	public static final class BooleanType extends RawJsonType {
	}

	@ToString(callSuper = true)
	@Getter
	public static final class IntegerType extends RawJsonType {
		private String format; // Could be int64 sometimes
	}

	@ToString(callSuper = true)
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static final class StringType extends RawJsonType {
		private @Nullable String format;
		private @Nullable String pattern;
		@JsonProperty("enum")
		private @Nullable List<String> validValues = null;
	}

	@ToString(callSuper = true)
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static final class ObjectType extends RawJsonType {
		private Map<String, MemberType> properties = new HashMap<>();
		private @Nullable MemberType additionalProperties = null;
		private Set<String> required = new HashSet<>();
	}
}
