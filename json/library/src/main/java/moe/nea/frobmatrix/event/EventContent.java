package moe.nea.frobmatrix.event;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class EventContent {
	private Map<String, JsonNode> contentFields = new HashMap<>();

	@JsonAnySetter
	@JsonAnyGetter
	public Map<String, JsonNode> contentFields() {
		return contentFields;
	}
}
