package moe.nea.frobmatrix.api.clientserver.routes.admin;

import moe.nea.frobmatrix.api.ResponseHelpers;
import moe.nea.frobmatrix.test.JsonMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static moe.nea.frobmatrix.test.JsonMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class GetWhoIsTest {
	@Test
	void checkDevicesAdditionalProperties() {
		var devices = new GetWhoIs.Response.Devices();
		devices.setAdditionalProperty("meow", new GetWhoIs.Response.Devices.DeviceInfo(
			List.of(new GetWhoIs.Response.Devices.DeviceInfo.SessionInfo(
				List.of(new GetWhoIs.Response.Devices.DeviceInfo.SessionInfo.ConnectionInfo(
					"127.0.0.1", 0L, "meow/ing 6.0"
				))
			))
		));
		var json = ResponseHelpers.toJsonValue(devices);
		assertThat(
			json,
			hasProperty("/meow/sessions/0/connections/0",
				Matchers.allOf(
					hasProperty("/user_agent", hasString(equalTo("meow/ing 6.0"))),
					hasProperty("/ip", hasString(equalTo("127.0.0.1")))
				))
		);
	}
}
