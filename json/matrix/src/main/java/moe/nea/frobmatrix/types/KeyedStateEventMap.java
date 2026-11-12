package moe.nea.frobmatrix.types;

import io.reactivex.rxjava3.core.Observable;
import moe.nea.frobmatrix.event.EventContent;
import moe.nea.frobmatrix.event.EventType;

import java.util.Optional;
import java.util.Set;

public interface KeyedStateEventMap extends StateEventMap {

	String DEFAULT_KEY = "";

	Observable<Set<String>> getStateKeys(EventType<?> stateEventType);

	<T extends EventContent<T>> Optional<T> getStateEvent(EventType<T> stateEventType, String stateEventKey);

	<T extends EventContent<T>> Observable<Optional<T>> observeStateEvent(EventType<T> stateEventType, String stateEventKey);

	@Override
	default <T extends EventContent<T>> Optional<T> getStateEvent(EventType<T> stateEventType) {
		return getStateEvent(stateEventType, DEFAULT_KEY);
	}

	@Override default <T extends EventContent<T>> Observable<Optional<T>> observeStateEvent(EventType<T> stateEventType) {
		return observeStateEvent(stateEventType, DEFAULT_KEY);
	}
}
