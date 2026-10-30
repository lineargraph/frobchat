package moe.nea.frobmatrix.api.clientserver.definitions.clienteventwithoutroomid;

import moe.nea.frobmatrix.api.ResponseHelpers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
		var eventContent = clientEvent.content();
	}
}
