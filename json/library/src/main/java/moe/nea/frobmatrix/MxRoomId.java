package moe.nea.frobmatrix;

import com.fasterxml.jackson.annotation.JsonValue;
import moe.nea.frobmatrix.api.UriParameter;

import java.util.regex.Pattern;

public record MxRoomId(
	String opaqueId
) implements UriParameter {
	public static final Pattern MX_ROOM_ID_PATTERN = Pattern.compile("!(.+)");

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
