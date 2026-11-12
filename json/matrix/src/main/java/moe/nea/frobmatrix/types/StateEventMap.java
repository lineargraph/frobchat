package moe.nea.frobmatrix.types;

import io.reactivex.rxjava3.core.Observable;
import moe.nea.frobmatrix.event.EventContent;
import moe.nea.frobmatrix.event.StateEventType;

import java.util.Optional;

public interface StateEventMap {
	<T extends EventContent<T>> Observable<Optional<T>> observeStateEvent(StateEventType<T> stateEventType);

	<T extends EventContent<T>> Optional<T> getStateEvent(StateEventType<T> stateEventType);
}
