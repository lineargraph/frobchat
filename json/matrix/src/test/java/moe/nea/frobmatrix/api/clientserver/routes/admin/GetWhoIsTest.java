package moe.nea.frobmatrix.api.clientserver.routes.admin;

import moe.nea.frobmatrix.api.ResponseHelpers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static moe.nea.frobmatrix.test.JsonMatchers.hasProperty;
import static moe.nea.frobmatrix.test.JsonMatchers.hasString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class GetWhoIsTest {
	@Test
	void checkDevicesAdditionalProperties() {
		var response = new GetWhoIs.Response("nea",
			Map.of("meow", new GetWhoIs.Response.DeviceInfo(
				List.of(new GetWhoIs.Response.DeviceInfo.SessionInfo(
					List.of(new GetWhoIs.Response.DeviceInfo.SessionInfo.ConnectionInfo(
						"127.0.0.1", 0L, "meow/ing 6.0"
					))
				))
			)));
		var json = ResponseHelpers.toJsonValue(response);
		assertThat(
			json,
			hasProperty("/devices/meow/sessions/0/connections/0",
				Matchers.allOf(
					hasProperty("/user_agent", hasString(equalTo("meow/ing 6.0"))),
					hasProperty("/ip", hasString(equalTo("127.0.0.1")))
				))
		);
	}
}
