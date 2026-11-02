package moe.nea.frobmatrix.eventschemas.schema.coreeventschema.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Override;
import java.lang.String;

import moe.nea.frobmatrix.event.RawEventContent;
import tools.jackson.databind.JsonNode;

public class Event implements moe.nea.frobmatrix.event.Event {
	@JsonProperty(
			required = true
	)
	private RawEventContent content;

	@JsonProperty(
			required = true
	)
	private String type;

	public Event() {
	}

	public Event(RawEventContent content, String type) {
		this.content = content;
		this.type = type;
	}

	public RawEventContent content() {
		return this.content;
	}

	public void setContent(RawEventContent content) {
		this.content = content;
	}

	public String type() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Event [ " + "content = " + this.content() + ", " + "type = " + this.type() + " ]";
	}
}
