package moe.nea.frobmatrix.types;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import moe.nea.frobmatrix.event.EventContent;
import moe.nea.frobmatrix.event.EventType;
import moe.nea.frobmatrix.event.ParsedEventContent;
import moe.nea.frobmatrix.event.StateEventType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StateEventMapImpl implements KeyedStateEventMap {
	private final Map<StateEventKey<?>, BehaviorSubject<Optional<?>>> roomState = new HashMap<>();

	record StateEventKey<T extends EventContent<T>>(
		EventType<T> stateEventType,
		String stateEventKey
	) {
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <T extends EventContent<T>> BehaviorSubject<Optional<T>> getStateEvent0(EventType<T> eventType, String stateEventKey) {
		var key = new StateEventKey<T>(
			eventType,
			stateEventKey
		);
		return (BehaviorSubject) roomState.computeIfAbsent(key, it -> BehaviorSubject.createDefault(Optional.empty()));
	}

	public <T extends EventContent<T>> Observable<Optional<T>> observeStateEvent(StateEventType<T> stateEventType, String stateEventKey) {
		return getStateEvent0(stateEventType, stateEventKey).hide();
	}

	public <T extends EventContent<T>> Optional<T> getStateEvent(StateEventType<T> stateEventType, String stateEventKey) {
		return getStateEvent0(stateEventType, stateEventKey).getValue();
	}


	public <T extends EventContent<T>> void setStateEvent0(ParsedEventContent<T> eventContent, String stateEventKey) {
		var stateEventBehaviour = getStateEvent0(eventContent.type(), stateEventKey);
		stateEventBehaviour.onNext(eventContent.value());
	}
}
