package moe.nea.frobjson.internal;

import com.google.gson.*;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@NullMarked
public class JsonUtil {
	private JsonUtil() {
	}

	public static <T extends @Nullable Object> @Nullable T catch_(Supplier<? extends T> supp) {
		try {
			return supp.get();
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static @Nullable String getStringOrNull(@Nullable JsonElement element) {
		if (element == null) return null;
		return element.getAsString();
	}

	public static @Nullable String getStringOrNull(@Nullable JsonElement element, String name) {
		if (element == null) return null;
		if (!(element instanceof JsonObject object)) return null;
		return getStringOrNull(object.get(name));
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

	public static <T extends @Nullable Object> void acceptNullable(T obj, Consumer<? super @NonNull T> action) {
		if (obj != null)
			action.accept(obj);
	}

	@Contract(mutates = "param1")
	public static <T extends JsonElement> T mergeVariant(T a, JsonElement b) {
		if (a instanceof JsonObject object) {
			for (var prop : b.getAsJsonObject().entrySet()) {
				var left = object.get(prop.getKey());
				var right = prop.getValue();
				if (left == null)
					object.add(prop.getKey(), right);
				else
					object.add(prop.getKey(), mergeVariant(left, right));
			}
			return a;
		}
		if (!a.equals(b)) throw new RuntimeException("Mismatched merging " + a + " <- " + b);
		return a;
	}

	@Contract(mutates = "param1")
	public static JsonElement mergeVariantOverriding(JsonElement a, JsonElement b) {
		if (a instanceof JsonObject object) {
			for (var prop : b.getAsJsonObject().entrySet()) {
				var left = object.get(prop.getKey());
				var right = prop.getValue();
				if (left == null)
					object.add(prop.getKey(), right);
				else
					object.add(prop.getKey(), mergeVariantOverriding(left, right));
			}
			return object;
		}
		return b;
	}

	public static final Gson GSON = new Gson();

	public static JsonElement loadJson(Path path) throws IOException {
		try (var input = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			return GSON.fromJson(input, JsonElement.class);
		}
	}

	public static JsonObject jsonObjectOrEmpty(@Nullable JsonElement content) {
		if (content == null)
			return new JsonObject();
		return content.getAsJsonObject();
	}

	public static boolean getBooleanOrFalse(@Nullable JsonElement element) {
		return element != null && element.getAsBoolean();
	}

	public static void appendQuery(Map<? super String, ? super String> queryParameters, String key, @Nullable Number value) {
		if (value != null)
			queryParameters.put(key, value.toString());
	}

	public static void appendQuery(Map<? super String, ? super String> queryParameters, String key, @Nullable Boolean value) {
		if (value != null)
			queryParameters.put(key, value.toString());
	}

	public static void appendQuery(Map<? super String, ? super String> queryParameters, String key, @Nullable String value) {
		if (value != null)
			queryParameters.put(key, value);
	}

	public static <T> Map<String, T> collectAdditionalProperties(JsonObject element, Function<? super JsonElement, ? extends T> mapper) {
		return element.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, it -> mapper.apply(it.getValue())));
	}

	public interface ThrowingProvider<T> {
		T provide() throws Throwable;
	}

	public static <T extends @Nullable Object, J extends @Nullable Object> T bind(J element, Function<? super J, ? extends T> decoder) {
		return decoder.apply(element);
	}

	public static <T, R> Function<@Nullable T, @Nullable R> liftNullable(Function<? super T, ? extends R> lambda) {
		return value -> value != null ? lambda.apply(value) : null;
	}

	public static <T> T iex(ThrowingProvider<T> provider) {
		try {
			return provider.provide();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static <T, C extends Collection<T>> Function<JsonElement, C> liftArray(Function<? super JsonElement, ? extends T> item, Collector<T, ?, C> collector) {
		return element -> stream(element.getAsJsonArray()).map(item).collect(collector);
	}

	public static <T> Function<? super Collection<? extends T>, JsonArray> liftUnArray(Function<? super T, ? extends JsonElement> item) {
		return element -> unstream(element.stream().map(item));
	}
}
