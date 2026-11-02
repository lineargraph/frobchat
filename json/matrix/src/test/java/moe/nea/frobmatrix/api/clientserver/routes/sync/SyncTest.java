package moe.nea.frobmatrix.api.clientserver.routes.sync;

import moe.nea.frobmatrix.MxRoomId;
import moe.nea.frobmatrix.MxUserId;
import moe.nea.frobmatrix.api.ResponseHelpers;
import moe.nea.frobmatrix.api.clientserver.definitions.clienteventwithoutroomid.ClientEventWithoutRoomId;
import moe.nea.frobmatrix.event.*;
import moe.nea.frobmatrix.event.types.message.EmoteMessage;
import moe.nea.frobmatrix.event.types.message.TextMessage;
import moe.nea.frobmatrix.http.UriBuilder;
import moe.nea.frobmatrix.util.IterUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SyncTest {

	@Test
	void testSimpleResponse() {
		Sync.Response response = ResponseHelpers.fromJsonStream(getClass().getResourceAsStream("/simple_example_sync.json"), Sync.Response.class);
		var dmsWithMyself = response.rooms().join()
			.additionalProperties()
			.get(new MxRoomId("IVeSfvhpVNHxhhhtKk:nea.moe"));
		var message = IterUtil.single(dmsWithMyself.timeline().events());
		var textMessage = (EmoteMessage) EventTypes.getContent(message.type(), message.content());
		assertThat(
			textMessage.body(),
			equalTo("purrs")
		);
		assertThat(
			message.sender(),
			equalTo(new MxUserId("nea", "nea.moe"))
		);
	}

	@Test
	void testEventTypes() {
		Sync.Response response;
		if (false) {
			List<ClientEventWithoutRoomId> leaveTimeline = response.rooms().leave()
				.additionalProperties().get(null)
				.timeline().events();
			var joinedRoom = response.rooms().join().additionalProperties().get(null);
			List<? extends Event> accountData = joinedRoom.accountData().events();
			List<ClientEventWithoutRoomId> stateEvents = joinedRoom.state().events();
			for (ClientEventWithoutRoomId event : joinedRoom
				.timeline()
				.events()) {
				RawEventContent eventContent = event.content();
				RawEventContent prevEventContent = event.unsigned().prevContent();
				ClientEventWithoutRoomId redactedBecause = event.unsigned().redactedBecause();
			}
		}
	}


	@Test
	void testOptionalParameters() {
		var sync = new Sync(
			null, // TODO: the sync parameter can be a JSON object, actually
			null,
			false,
			null,
			3000,
			true
		);
		assertThat(
			sync.appendUriTo(new UriBuilder("https", null)).build().toString(),
			allOf(
				containsString("use_state_after=true"),
				not(containsString("since"))
			)
		);
	}

}
