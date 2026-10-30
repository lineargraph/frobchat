package moe.nea.frobmatrix.event;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

public class RawEventContent {
	private JsonNode contentFields = new ObjectNode(JsonNodeFactory.instance);

	@JsonAnySetter
	@JsonAnyGetter
	public JsonNode contentFields() {
		return contentFields;
	}
}
