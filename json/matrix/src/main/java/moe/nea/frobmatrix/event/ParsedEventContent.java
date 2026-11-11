package moe.nea.frobmatrix.event;

import java.util.Optional;

public record ParsedEventContent<T extends EventContent<T>>(
	EventType<T> type,
	Optional<T> value
) {
}
