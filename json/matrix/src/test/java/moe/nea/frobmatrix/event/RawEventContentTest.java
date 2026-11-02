package moe.nea.frobmatrix.event;

import moe.nea.frobmatrix.api.ResponseHelpers;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class RawEventContentTest {
	@Test
	void testRawEventSerialization() {
		JsonNode node = ResponseHelpers.fromJsonString("""
			{
				"meow": true
			}
			""", JsonNode.class);
		RawEventContent rawEventContent = ResponseHelpers.fromJsonNode(node, RawEventContent.class);
		assertThat(
			node,
			equalTo(rawEventContent.contentFields())
		);
	}
}
