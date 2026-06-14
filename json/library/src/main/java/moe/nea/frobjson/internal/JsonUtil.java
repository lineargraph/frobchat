package moe.nea.frobjson.internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonUtil {
	private JsonUtil() {
	}

	public static @Nullable String getStringOrNull(@Nullable JsonElement element) {
		if (element == null) return null;
		return element.getAsString();
	}

	public static Stream<Map.Entry<String, JsonElement>> streamEntries(JsonObject object) {
		return object.entrySet().stream();
	}

	public static Stream<Map.Entry<String, JsonElement>> streamEntriesOrEmpty(@Nullable JsonElement element) {
		if (element == null) return Stream.empty();
		return streamEntries((JsonObject) element);
	}

	public static Stream<JsonElement> stream(JsonArray array) {
		return StreamSupport.stream(array.spliterator(), false);
	}

	public static Stream<JsonElement> streamOrEmpty(@Nullable JsonElement element) {
		if (element == null) return Stream.empty();
		return stream((JsonArray) element);
	}

	public interface ThrowingProvider<T> {
		T provide() throws Throwable;
	}

	public static <T> T iex(ThrowingProvider<T> provider) {
		try {
			return provider.provide();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
