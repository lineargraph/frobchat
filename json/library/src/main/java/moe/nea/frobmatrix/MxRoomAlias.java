package moe.nea.frobmatrix;

import com.fasterxml.jackson.annotation.JsonValue;
import moe.nea.frobmatrix.api.UriParameter;

import java.util.regex.Pattern;

public record MxRoomAlias(
	String name,
	String homeserver
) implements UriParameter {
	public static final Pattern MX_ROOM_ALIAS_PATTERN = Pattern.compile("#(.+):(.+)");

	public static MxRoomAlias parse(String mxcId) {
		var matcher = MX_ROOM_ALIAS_PATTERN.matcher(mxcId);
		if (matcher.matches())
			return new MxRoomAlias(matcher.group(1), matcher.group(2));
		throw new RuntimeException("Failed to parse mx-room-alias: " + mxcId);
	}

	@Override
	@JsonValue
	public String asUriParameter() {
		return "#" + name + ":" + homeserver;
	}
}
