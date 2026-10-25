package moe.nea.frobmatrix;

import moe.nea.frobmatrix.api.ResponseHelpers;
import org.junit.jupiter.api.Test;

import static moe.nea.frobmatrix.test.JsonMatchers.hasString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class MxUserIdTest {
	@Test
	void testSerialize() {
		var mxIdJson = ResponseHelpers.toJsonValue(new MxUserId("nea", "nea.moe"));
		assertThat(
			mxIdJson,
			hasString(equalTo("@nea:nea.moe"))
		);
	}
}
