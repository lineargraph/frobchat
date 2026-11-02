package moe.nea.frobmatrix.api.clientserver.definitions.clienteventwithoutroomid;

import moe.nea.frobmatrix.MxEventId;
import moe.nea.frobmatrix.api.ResponseHelpers;
import moe.nea.frobmatrix.event.EventTypes;
import moe.nea.frobmatrix.event.RawEventContent;
import moe.nea.frobmatrix.event.types.message.TextMessage;
import moe.nea.frobmatrix.test.JsonMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ClientEventWithoutRoomIdTest {
	@Test
	void simpleTextMessage() {
		var json = """
			{
			  "content": {
			    "body": "nya",
			    "m.mentions": {},
			    "msgtype": "m.text"
			  },
			  "event_id": "$mMHjt3fUK7nyjKhIpk8CkenhUE9YTCUj-WAzAwbDKfI",
			  "origin_server_ts": 1793394278587,
			  "sender": "@nea:nea.moe",
			  "type": "m.room.message",
			  "unsigned": {
			    "age": 552,
			    "membership": "join",
			    "transaction_id": "m1793394278429.24"
			  },
			  "room_id": "!oY0Y4kgb3boFnEhWvHZGsVNK1ZkHgV9LA7rjl28j95Y"
			}
			""";
		var clientEvent = ResponseHelpers.fromJsonString(json, ClientEventWithoutRoomId.class);
		RawEventContent eventContent = clientEvent.content();
		assertThat(
			eventContent.contentFields(),
			JsonMatchers.hasProperty("/msgtype", JsonMatchers.hasString(equalTo("m.text")))
		);
		var content = (TextMessage) EventTypes.getContent(clientEvent.type(), clientEvent.content());
		assertThat(
			content.body(),
			equalTo("nya")
		);
		assertThat(
			content.msgtype(),
			equalTo("m.text")
		);
		assertThat(
			clientEvent.eventId(),
			equalTo(new MxEventId("mMHjt3fUK7nyjKhIpk8CkenhUE9YTCUj-WAzAwbDKfI"))
		);
	}
}
