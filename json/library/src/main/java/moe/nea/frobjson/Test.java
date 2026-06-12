package moe.nea.frobjson;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import moe.nea.frobjson.model.DiscoveryInformation;
import moe.nea.frobjson.model.HomeserverInformation;

public class Test {

	static void main() {
		System.out.println(new DiscoveryInformation(
			new HomeserverInformation(
				"https://matrix.nea.moe"
			),
			null
		).asJson());
		var json = new Gson().fromJson("""
			{
					"m.homeserver": {
						"base_url": "https://matrix.example.com"
					},
					"m.identity_server": {
						"base_url": "https://identity.example.com"
					},
					"org.example.custom.property": {
						"app_url": "https://custom.app.example.org"
					}
				}
			""", JsonObject.class);
		System.out.println(DiscoveryInformation.fromJson(json));
		System.out.println(DiscoveryInformation.fromJson(json).asJson());
	}
}
