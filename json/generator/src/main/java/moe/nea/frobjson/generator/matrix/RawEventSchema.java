package moe.nea.frobjson.generator.matrix;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.javapoet.ClassName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import moe.nea.frobjson.internal.IterUtil;
import moe.nea.frobmatrix.event.EventType;
import moe.nea.frobmatrix.event.StateEventType;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/// Fake class that wraps all of the interesting bits of the event schema declaration
@Getter
@Setter
@ToString
@JsonIgnoreProperties({"$schema"})
public class RawEventSchema {
	private String description;
	private String title;
	private String type; // Must be object
	private List<AllOf> allOf;
	private Properties properties;
	private List<String> required;

	public ClassName stateEventType() {
		var refType = IterUtil.single(getAllOf()).getReference();
		return switch (refType) {
			case "core-event-schema/state_event.yaml" -> ClassName.get(StateEventType.class);
			case "core-event-schema/event.yaml" -> ClassName.get(EventType.class);
			default -> throw new RuntimeException("Unknown parent reference " + refType);
		};
	}

	@Getter
	@Setter
	@ToString
	public static class Properties {
		private RawJsonType.ObjectType content;
		@JsonProperty("state_key")
		private @Nullable MemberType stateKey;
		private StringEnumDeclaration type;
	}

	@Getter
	@Setter
	@ToString
	public static class StringEnumDeclaration {
		@JsonProperty("enum")
		private List<String> values;
		private String type; // Must be string

		public String getValue() {
			return IterUtil.single(values);
		}
	}

	@Getter
	@Setter
	@ToString
	public static class AllOf {
		@JsonProperty("$ref")
		private String reference;
	}
}
