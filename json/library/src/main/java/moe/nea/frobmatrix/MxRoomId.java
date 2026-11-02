package moe.nea.frobmatrix;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import moe.nea.frobmatrix.api.UriParameter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.KeyDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.regex.Pattern;

@JsonDeserialize(
	keyUsing = MxRoomId.MxRoomIdKeyDeserializer.class
)
public record MxRoomId(
	String opaqueId
) implements UriParameter {
	public static final Pattern MX_ROOM_ID_PATTERN = Pattern.compile("!(.+)");

	public static class MxRoomIdKeyDeserializer extends KeyDeserializer {
		@Override
		public Object deserializeKey(String key, DeserializationContext ctxt) throws JacksonException {
			return MxRoomId.parse(key);
		}
	}

	@JsonCreator
	public static MxRoomId parse(String mxcId) {
		var matcher = MX_ROOM_ID_PATTERN.matcher(mxcId);
		if (matcher.matches())
			return new MxRoomId(matcher.group(1));
		throw new RuntimeException("Failed to parse mx-room-id: " + mxcId);
	}


	@Override
	@JsonValue
	public String asUriParameter() {
		return "!" + opaqueId;
	}
}
