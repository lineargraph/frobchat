package moe.nea.frobmatrix;

import java.util.regex.Pattern;

public record MxUserId(
	String name,
	String homeserver
) {
	public static Pattern MX_USER_ID_PATTERN = Pattern.compile("@(.+):(.+)");

	public static MxUserId parse(String mxcId) {
		var matcher = MX_USER_ID_PATTERN.matcher(mxcId);
		if (matcher.matches())
			return new MxUserId(matcher.group(1), matcher.group(2));
		throw new RuntimeException("Failed to parse mx-user-id: " + mxcId);
	}
}
