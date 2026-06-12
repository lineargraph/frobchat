package moe.nea.frobjson;

import com.google.gson.*;
import org.jspecify.annotations.Nullable;

public final class WellKnownHomeserverInformation {
	private String base_url;

	public String getBase_url() {
		return base_url;
	}


	private WellKnownHomeserverInformation() {
	}

	public static WellKnownHomeserverInformation fromJson(JsonElement element) {
		var obj = element.getAsJsonObject();
		var wellKnownHomeserverInformation = new WellKnownHomeserverInformation();
		wellKnownHomeserverInformation.jsonElement = element;
		wellKnownHomeserverInformation.base_url = obj.get("base_url").getAsString();
		return wellKnownHomeserverInformation;
	}

	private @Nullable JsonElement jsonElement;

	private JsonElement generateJsonElement() {
		JsonObject object = new JsonObject();
		object.add("base_url", new JsonPrimitive(base_url));
		return object;
	}

	public JsonElement asJson() {
		if (jsonElement == null)
			return jsonElement = generateJsonElement();
		return jsonElement;
	}
}
