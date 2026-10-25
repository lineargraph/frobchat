package moe.nea.frobmatrix.api.clientserver.routes.roomsummary;

import moe.nea.frobmatrix.Either;
import moe.nea.frobmatrix.MxRoomAlias;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class GetRoomSummaryTest {
	@Test
	void getRoomSummary() {
		var summary = new GetRoomSummary(
			Either.ofRight(
				new MxRoomAlias("scrolling-space", "matrix.org")
			),
			List.of("nea.moe", "zimward.moe")
		);
		assertThat(
			summary.path(),
			equalTo("/_matrix/client/v1/room_summary/%23scrolling-space%3Amatrix.org?via=nea.moe&via=zimward.moe")
		);
	}
}
