package moe.nea.frobmatrix.event.types;

import moe.nea.frobmatrix.api.ResponseHelpers;
import moe.nea.frobmatrix.event.types.message.TextMessage;
import moe.nea.frobmatrix.test.JsonMatchers;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static moe.nea.frobmatrix.test.JsonMatchers.hasProperty;
import static moe.nea.frobmatrix.test.JsonMatchers.hasString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class MessageTest {
	@Test
	void testUnknownEvent() {
		JsonNode node = ResponseHelpers.fromJsonString("""
			{
			  "msgtype": "moe.nea.unassigned",
			  "custom_field": "i genuinely do not know",
			  "body": "exists!"
			}
			""", JsonNode.class);
		Message message = ResponseHelpers.fromJsonNode(node, Message.class);
		assertThat(
			ResponseHelpers.toJsonValue(message),
			equalTo(node)
		);
		assertThat(
			message.body(),
			equalTo("exists!")
		);
		assertThat(
			message.msgtype(),
			equalTo("moe.nea.unassigned")
		);
	}

	@Test
	void testEncodeMessage() {
		var textMessage = new TextMessage("m.plain", "meow");
		assertThat(
			ResponseHelpers.toJsonValue(textMessage),
			hasProperty("/msgtype", hasString(equalTo("m.text"))));
	}

	@Test
	void testDecodeMessage() {
		var textMessage = (TextMessage) ResponseHelpers.fromJsonString(
			"""
				{
				   "body": "nya",
				   "m.mentions": {},
				   "msgtype": "m.text"
				}
				""",
			Message.class
		);
		assertThat(
			textMessage.msgtype(),
			equalTo("m.text"));
		assertThat(
			textMessage.body(),
			equalTo("nya"));
	}
}
