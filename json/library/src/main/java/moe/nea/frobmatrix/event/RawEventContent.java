package moe.nea.frobmatrix.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

public class RawEventContent {
	private final ObjectNode contentFields;

	public ObjectNode contentFields() {
		return contentFields;
	}

	public RawEventContent() {
		this.contentFields = new ObjectNode(JsonNodeFactory.instance);
	}

	@JsonCreator
	public RawEventContent(ObjectNode json) {
		this.contentFields = json;
	}

	@JsonValue
	public JsonNode asJson() {
		return contentFields;
	}
}
