package moe.nea.frobmatrix.api.clientserver.routes.login;


import moe.nea.frobmatrix.api.ResponseHelpers;
import moe.nea.frobmatrix.api.clientserver.definitions.useridentifier.UserIdentifier;
import moe.nea.frobmatrix.api.clientserver.definitions.wellknown.full.DiscoveryInformation;
import org.junit.jupiter.api.Test;

import static moe.nea.frobmatrix.test.JsonMatchers.hasProperty;
import static moe.nea.frobmatrix.test.JsonMatchers.hasString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

class LoginTest {
	@Test
	void whatever() {
		var requestBody = new Login.RequestBody("m.password");
		requestBody.setIdentifier(new UserIdentifier.MatrixUserID("test"));
		requestBody.setPassword("meow");
		var login = new Login(requestBody);
		var requestBodyJson = ResponseHelpers.toJsonValue(requestBody);
		assertThat(
			requestBodyJson,
			allOf(
				hasProperty("/identifier/type", hasString(equalTo("m.id.user"))),
				hasProperty("/identifier/user", hasString(equalTo("test"))),
				hasProperty("/password", hasString(equalTo("meow")))
			)
		);

		Login.Response response = null;
		if (false) {
			DiscoveryInformation discoveryInformation = response.wellKnown();
		}
	}
}
