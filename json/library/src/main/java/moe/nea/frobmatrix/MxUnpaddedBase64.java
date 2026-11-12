package moe.nea.frobmatrix;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record MxUnpaddedBase64(
	String base64Encoded
) {
	@JsonCreator
	public static MxUnpaddedBase64 parse(String str) {
		return new MxUnpaddedBase64(str); // TODO: actually store this as
	}

	@JsonValue
	public String base64Encoded() {
		return base64Encoded;
	}
}
