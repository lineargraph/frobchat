package moe.nea.frobmatrix.event;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class BaseEventContent {
	private final Map<String, JsonNode> extraFields = new HashMap<>();

	@JsonAnySetter
	public void setExtraField(String name, JsonNode node) {
		extraFields.put(name, node);
	}

	@JsonAnyGetter
	public Map<String, JsonNode> getExtraFields() {
		return extraFields;
	}

}
