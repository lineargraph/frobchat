package moe.nea.frobmatrix.api.clientserver.definitions.wellknown.full;

import moe.nea.frobmatrix.api.ResponseHelpers;
import moe.nea.frobmatrix.test.JsonMatchers;
import org.junit.jupiter.api.Test;

import static moe.nea.frobmatrix.test.JsonMatchers.hasProperty;
import static moe.nea.frobmatrix.test.JsonMatchers.hasString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class DiscoveryInformationTest {

	@Test
	void testAdditionalProperties() {
		var json = """
			{
			  "m.homeserver": {"base_url": "https://matrix.nea.moe"},
			  "org.matrix.msc4143.rtc_foci": [
			    {
			      "livekit_service_url": "https://matrix.nea.moe/livekit/jwt",
			      "type": "livekit"
			    }
			  ]
			}
			""";
		var discoveryInformation = ResponseHelpers.fromJsonString(json, DiscoveryInformation.class);
		assertThat(
			discoveryInformation.homeserver().baseUrl().toString(),
			equalTo("https://matrix.nea.moe")
		);
		assertThat(
			discoveryInformation.additionalProperties(),
			hasEntry(
				equalTo("org.matrix.msc4143.rtc_foci"),
				hasItem(allOf(
					hasProperty("/livekit_service_url", hasString(equalTo("https://matrix.nea.moe/livekit/jwt"))),
					hasProperty("/type", hasString(equalTo("livekit")))
				))
			)
		);

	}

}
