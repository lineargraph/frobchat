package moe.nea.frobmatrix.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Event {
	public EventContent content() {
		return content;
	}

	public String type() {
		return type;
	}

	@JsonProperty(required = true)
	private EventContent content;
	@JsonProperty(required = true)
	private String type;
}
