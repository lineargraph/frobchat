package moe.nea.frobmatrix.event.types;

import moe.nea.frobmatrix.MxUserId;
import moe.nea.frobmatrix.api.ResponseHelpers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

class DirectChatMappingTest {
	@Test
	void testDirectMessageExtraFields() {
		DirectChatMapping mapping = ResponseHelpers.fromJsonString("""
			{
			  "@nea:nea.moe": [
				"!IVeSfvhpVNHxhhhtKk:nea.moe"
			  ]
			}
			""", DirectChatMapping.class);
		assertThat(
			mapping.additionalProperties(),
			Matchers.hasEntry(
				new MxUserId("nea", "nea.moe"),
				List.of("!IVeSfvhpVNHxhhhtKk:nea.moe")
			)
		);
		assertThat(mapping.getExtraFields(), Matchers.anEmptyMap());

		var roundTripped = ResponseHelpers.fromJsonString(
			ResponseHelpers.toJsonValue(mapping).toString(),
			DirectChatMapping.class);
		assertThat(
			roundTripped.additionalProperties(),
			Matchers.hasEntry(
				new MxUserId("nea", "nea.moe"),
				List.of("!IVeSfvhpVNHxhhhtKk:nea.moe")
			)
		);
		assertThat(roundTripped.getExtraFields(), Matchers.anEmptyMap());
	}

}
