package moe.nea.frobjson.generator.matrix;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	@JsonSubTypes.Type(value = RawJsonType.ObjectType.class, name = "object")
})
public sealed abstract class RawJsonType {
	private @Nullable String title;
	private @Nullable String description;
	private @Nullable JsonNode example;

	@ToString(callSuper = true)
	public static final class StringType extends RawJsonType {

	}

	@ToString(callSuper = true)
	@Getter
	public static final class ObjectType extends RawJsonType {
		private Map<String, RawJsonType> properties = new HashMap<>();
		private List<String> required = new ArrayList<>();
	}
}
