package moe.nea.frobjson.openapi;

import moe.nea.frobjson.internal.SchemaObject;
import moe.nea.frobjson.openapi.client.JavaHttpClient;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryParameters {
	private final Map<String, Object> queryParameters = new HashMap<>();

	public static QueryParameters empty() {
		return new QueryParameters();
	}

	public @Nullable Object get(String key) {
		return queryParameters.get(key);
	}

	public List<String> getList(String key) {
		var value = get(key);
		if (value == null) return List.of();
		if (value instanceof String str) return List.of(str);
		//noinspection unchecked
		return (List<String>) value;
	}

	public @Nullable String getFirst(String key) {
		var list = getList(key);
		if (list.isEmpty()) return null;
		return list.getFirst();
	}

	public void putSingle(String key, String value) {
		queryParameters.put(key, value);
	}

	public void putList(String key, List<String> value) {
		queryParameters.put(key, value);
	}

	public void put(String key, @Nullable List<String> value) {
		if (value == null) return;
		putList(key, value);
	}

	// TODO: work out something more solid here
	public void put(String key, @Nullable SchemaObject value) {
		if (value == null) return;
		putSingle(key, value.asJson().toString());
	}

	public void put(String key, @Nullable String value) {
		if (value == null) return;
		putSingle(key, value);
	}

	public void put(String key, @Nullable Number number) {
		if (number == null) return;
		putSingle(key, number.toString());
	}

	public void put(String key, @Nullable Boolean bool) {
		if (bool == null) return;
		putSingle(key, bool.toString());
	}

	public String asQueryString() {
		if (queryParameters.isEmpty()) return "";
		//noinspection unchecked
		return queryParameters.entrySet().stream()
			.flatMap(it ->
				it.getValue() instanceof String value
					? Stream.of(JavaHttpClient.urlEncode(it.getKey()) + "=" + JavaHttpClient.urlEncode(value))
					: ((List<String>) it.getValue()).stream()
					.map(value -> JavaHttpClient.urlEncode(it.getKey()) + "[]=" + JavaHttpClient.urlEncode(value))
			)
			.collect(Collectors.joining("&", "?", ""));
	}
}
