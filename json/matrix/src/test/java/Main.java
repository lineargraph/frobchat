import com.google.gson.Gson;
import com.google.gson.JsonObject;
import moe.nea.frobchat.model.DiscoveryInformation;

void main() {
	var discoveryInformation = DiscoveryInformation.fromJson(
		new Gson().fromJson("""
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
			""", JsonObject.class)
	);
	System.out.println(discoveryInformation);
	System.out.println(discoveryInformation.mhomeserver());
	System.out.println(discoveryInformation.asJson());
	System.out.println(discoveryInformation.generateJson());
}
