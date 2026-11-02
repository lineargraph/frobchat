package moe.nea.frobmatrix.event;

import moe.nea.frobmatrix.api.ResponseHelpers;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@NullMarked
public class EventTypes {
	public static List<? extends EventType<?>> TYPES = EventTypesData.TYPES;
	public static Map<String, EventType<?>> INDEXED_TYPES = TYPES
		.stream().collect(Collectors.toMap(EventType::type, Function.identity()));

	public static @Nullable EventContent<?> getContent(
		String type,
		RawEventContent content
	) {
		var eventType = INDEXED_TYPES.get(type);
		if (eventType == null) return null;
		return getContent(content.contentFields(), eventType.contentType());
	}

	private static <T extends EventContent<T>> T getContent(JsonNode node, Class<T> tClass) {
		return ResponseHelpers.fromJsonNode(node, tClass);
	}
}
