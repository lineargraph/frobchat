package moe.nea.frobmatrix.types;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import moe.nea.frobmatrix.event.EventContent;
import moe.nea.frobmatrix.event.EventType;
import moe.nea.frobmatrix.event.ParsedEventContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StateEventMapImpl implements KeyedStateEventMap {
	private static final Logger log = LoggerFactory.getLogger(StateEventMapImpl.class);
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

	public <T extends EventContent<T>> Observable<Optional<T>> observeStateEvent(EventType<T> stateEventType, String stateEventKey) {
		return getStateEvent0(stateEventType, stateEventKey).hide();
	}

	public <T extends EventContent<T>> Optional<T> getStateEvent(EventType<T> stateEventType, String stateEventKey) {
		return getStateEvent0(stateEventType, stateEventKey).getValue();
	}


	public <T extends EventContent<T>> void setStateEvent0(ParsedEventContent<T> eventContent, String stateEventKey) {
		var stateEventBehaviour = getStateEvent0(eventContent.type(), stateEventKey);
		log.debug("Setting state of type {}", eventContent.type());
		stateEventBehaviour.onNext(eventContent.value());
	}


	public void setStateEvent(Optional<ParsedEventContent<?>> parsedEventContent, String stateKey) {
		parsedEventContent.ifPresent(content ->
			setStateEvent0(content, stateKey));
	}

}
