package moe.nea.frobmatrix.event;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BaseEventContent {
	@JsonAnySetter
	@JsonAnyGetter
	private @Nullable Map<String, JsonNode> extraFields = null;

	public void setExtraField(String name, JsonNode node) {
		if (extraFields == null)
			extraFields = new HashMap<>();
		extraFields.put(name, node);
	}

	@JsonIgnore
	public Map<String, JsonNode> getExtraFields() {
		if (extraFields == null)
			return Collections.emptyMap();
		return Collections.unmodifiableMap(extraFields);
	}
}
