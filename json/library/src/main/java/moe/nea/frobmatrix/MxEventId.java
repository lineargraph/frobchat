package moe.nea.frobmatrix;

import com.fasterxml.jackson.annotation.JsonValue;
import moe.nea.frobmatrix.api.UriParameter;

import java.util.regex.Pattern;

public record MxEventId(
	String opaqueId
) implements UriParameter {
	public static final Pattern MX_EVENT_ID_PATTERN = Pattern.compile("\\$(.+)");

	public static MxEventId parse(String mxcId) {
		var matcher = MX_EVENT_ID_PATTERN.matcher(mxcId);
		if (matcher.matches())
			return new MxEventId(matcher.group(1));
		throw new RuntimeException("Failed to parse mx-event-id: " + mxcId);
	}

	@Override
	@JsonValue
	public String asUriParameter() {
		return "$" + opaqueId;
	}
}
