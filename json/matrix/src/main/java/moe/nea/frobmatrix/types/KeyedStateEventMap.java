package moe.nea.frobmatrix.types;

import io.reactivex.rxjava3.core.Observable;
import moe.nea.frobmatrix.event.EventContent;
import moe.nea.frobmatrix.event.StateEventType;

import java.util.Optional;

public interface KeyedStateEventMap extends StateEventMap {

	<T extends EventContent<T>> Optional<T> getStateEvent(StateEventType<T> stateEventType, String stateEventKey);

	<T extends EventContent<T>> Observable<Optional<T>> observeStateEvent(StateEventType<T> stateEventType, String stateEventKey);

	@Override
	default <T extends EventContent<T>> Optional<T> getStateEvent(StateEventType<T> stateEventType) {
		return getStateEvent(stateEventType, "");
	}

	@Override default <T extends EventContent<T>> Observable<Optional<T>> observeStateEvent(StateEventType<T> stateEventType) {
		return observeStateEvent(stateEventType, "");
	}
}
