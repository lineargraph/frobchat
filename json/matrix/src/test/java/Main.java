import com.google.gson.Gson;
import com.google.gson.JsonObject;
import moe.nea.frobchat.model.Contact;

import moe.nea.frobchat.model.WellKnownProperties;

import java.util.List;

void main() {
	var wellKnownProperties = new WellKnownProperties(
		List.of(new Contact("nea@nea.moe", null, "admin")),
		"https://matrix.nea.moe"
	);
	IO.println(wellKnownProperties);
	IO.println(wellKnownProperties.asJson());
	IO.println(WellKnownProperties.fromJson(wellKnownProperties.asJson()));
}
