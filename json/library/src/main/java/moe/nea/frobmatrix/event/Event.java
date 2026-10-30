package moe.nea.frobmatrix.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Event {
	@JsonProperty(required = true)
	private EventContent content;
	@JsonProperty(required = true)
	private String type;
}
