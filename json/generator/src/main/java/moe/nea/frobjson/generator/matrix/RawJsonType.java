package moe.nea.frobjson.generator.matrix;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	@JsonSubTypes.Type(value = RawJsonType.UnknownType.class, name = "unknown"),
	@JsonSubTypes.Type(value = RawJsonType.ObjectType.class, name = "object")
})
public sealed abstract class RawJsonType {
	private @Nullable String title;
	private @Nullable String description;
	private @Nullable JsonNode example;

	// Pseudo type for "missing" types. Most likely this is for free form types
	@ToString(callSuper = true)
	@Getter
	public static final class UnknownType extends RawJsonType {
	}

	@ToString(callSuper = true)
	@Getter
	public static final class StringType extends RawJsonType {
		private @Nullable String format;
	}

	@ToString(callSuper = true)
	@Getter
	public static final class ObjectType extends RawJsonType {
		private Map<String, MemberType> properties = new HashMap<>();
		private @Nullable MemberType additionalProperties = null;
		private Set<String> required = new HashSet<>();
	}
}
