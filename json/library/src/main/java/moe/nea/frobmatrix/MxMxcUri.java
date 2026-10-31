package moe.nea.frobmatrix;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public record MxMxcUri(String host, String opaqueId) {
	public static final Pattern VALID_ID_PATTERN = Pattern.compile("[A-Za-z0-9_\\-]+");

	public MxMxcUri {
		assert VALID_ID_PATTERN.matcher(opaqueId).matches();
	}

	@JsonCreator
	public static MxMxcUri fromUri(URI uri) {
		assert uri.getScheme().equals("mxc");
		return new MxMxcUri(uri.getHost(), uri.getPath());
	}

	@JsonValue
	public URI asUri() {
		try {
			return new URI(
				"mxc",
				host,
				opaqueId,
				null
			);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
