package moe.nea.frobmatrix.api.clientserver.routes.sync;

import moe.nea.frobmatrix.api.clientserver.definitions.clienteventwithoutroomid.ClientEventWithoutRoomId;
import moe.nea.frobmatrix.event.Event;
import moe.nea.frobmatrix.event.EventContent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SyncTest {
	@Test
	void testEventTypes() {
		Sync.Response response;
		if (false) {
			List<ClientEventWithoutRoomId> leaveTimeline = response.rooms().leave()
				.additionalProperties().get(null)
				.timeline().events();
			var joinedRoom = response.rooms().join().additionalProperties().get(null);
			List<Event> accountData = joinedRoom.accountData().events();
			List<ClientEventWithoutRoomId> stateEvents = joinedRoom.state().events();
			for (ClientEventWithoutRoomId event : joinedRoom
				.timeline()
				.events()) {
				EventContent eventContent = event.content();
				EventContent prevEventContent = event.unsigned().prevContent();
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
			sync.path(),
			allOf(
				containsString("use_state_after=true"),
				not(containsString("since"))
			)
		);
	}

}
