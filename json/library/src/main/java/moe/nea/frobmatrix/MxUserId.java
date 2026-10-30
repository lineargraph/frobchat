package moe.nea.frobmatrix;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import moe.nea.frobmatrix.api.UriParameter;
import org.jspecify.annotations.NonNull;

import java.util.regex.Pattern;

public record MxUserId(
	String name,
	String homeserver
) implements UriParameter {
	public static Pattern MX_USER_ID_PATTERN = Pattern.compile("@(.+):(.+)");

	@JsonCreator
	public static MxUserId parse(String mxcId) {
		var matcher = MX_USER_ID_PATTERN.matcher(mxcId);
		if (matcher.matches())
			return new MxUserId(matcher.group(1), matcher.group(2));
		throw new RuntimeException("Failed to parse mx-user-id: " + mxcId);
	}

	@Override
	@JsonValue
	public String asUriParameter() {
		return "@" + name + ":" + homeserver;
	}
}
