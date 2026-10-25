package moe.nea.frobmatrix.api.clientserver.routes.login;


import moe.nea.frobmatrix.api.clientserver.definitions.wellknown.full.DiscoveryInformation;
import org.junit.jupiter.api.Test;

class LoginTest {
	@Test
	void whatever() {
		var login = new Login(
			new Login.RequestBody(
				"m.password"
			)
		);
		Login.Response response = null;
		if (false) {
			DiscoveryInformation discoveryInformation = response.wellKnown();
		}
	}
}
