package moe.nea.frobjson.internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
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

	public static JsonArray unstream(Stream<? extends JsonElement> stream) {
		var jsonArray = new JsonArray();
		stream.forEach(jsonArray::add);
		return jsonArray;
	}

	public static Stream<JsonElement> streamOrEmpty(@Nullable JsonElement element) {
		if (element == null) return Stream.empty();
		return stream((JsonArray) element);
	}

	public interface ThrowingProvider<T> {
		T provide() throws Throwable;
	}

	public static <T, J> T bind(J element, Function<? super J, ? extends T> decoder) {
		return decoder.apply(element);
	}

	public static <T, R> Function<@Nullable T, @Nullable R> liftNullable(Function<? super T, ? extends R> lambda) {
		return value -> value != null ? lambda.apply(value) : null;
	}

	public static <T, J> @Nullable T bindNullable(@Nullable J element, Function<? super J, ? extends T> decoder) {
		if (element == null) return null;
		return decoder.apply(element);
	}

	public static <T> T iex(ThrowingProvider<T> provider) {
		try {
			return provider.provide();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
