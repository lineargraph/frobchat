package moe.nea.frobmatrix.event;

import moe.nea.frobmatrix.api.ResponseHelpers;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@NullMarked
public class EventTypes {
	private static final Logger log = LoggerFactory.getLogger(EventTypes.class);
	public static List<? extends EventType<?>> TYPES = EventTypesData.TYPES;
	public static Map<String, EventType<?>> INDEXED_TYPES = TYPES
		.stream().collect(Collectors.toMap(EventType::type, Function.identity()));

	public static Optional<ParsedEventContent<?>> parseContent(String type, RawEventContent content) {
		var eventType = INDEXED_TYPES.get(type);
		if (eventType == null) return Optional.empty();
		return Optional.of(parseContent(content.contentFields(), eventType));
	}

	private static <T extends EventContent<T>> ParsedEventContent<T> parseContent(
		JsonNode node, EventType<T> eventType
	) {
		try {
			return new ParsedEventContent<>(eventType, Optional.of(getContent(node, eventType.contentType())));
		} catch (JacksonException e) {
			log.warn("Failed to parse state event of type {}: {}", eventType, node);
			return new ParsedEventContent<>(eventType, Optional.empty());
		}
	}

	@Deprecated
	public static @Nullable EventContent<?> getContent(
		String type,
		RawEventContent content
	) {
		var eventType = INDEXED_TYPES.get(type);
		if (eventType == null) return null;
		return getContent(content.contentFields(), eventType.contentType());
	}

	private static <T extends EventContent<T>> T getContent(JsonNode node, Class<T> tClass) throws JacksonException {
		return ResponseHelpers.fromJsonNode(node, tClass);
	}
}
