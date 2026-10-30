package moe.nea.frobmatrix.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import moe.nea.frobmatrix.MxUserId;


public class StrippedStateEvent extends Event {
	@JsonProperty(required = true)
	private MxUserId sender;
	@JsonProperty(required = true, value = "state_key")
	private String stateKey;

}
