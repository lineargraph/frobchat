package moe.nea.frobmatrix.types.room;

import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import moe.nea.frobmatrix.MatrixClient;
import moe.nea.frobmatrix.MxRoomId;
import moe.nea.frobmatrix.api.clientserver.definitions.clienteventwithoutroomid.ClientEventWithoutRoomId;
import moe.nea.frobmatrix.event.EventContent;
import moe.nea.frobmatrix.event.EventType;
import moe.nea.frobmatrix.event.EventTypes;
import moe.nea.frobmatrix.event.StateEventType;
import moe.nea.frobmatrix.event.types.Message;
import moe.nea.frobmatrix.types.event.MessageEventImpl;
import moe.nea.frobmatrix.types.event.RoomEvent;
import moe.nea.frobmatrix.types.event.RoomEventImpl;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NullMarked
public class MatrixRoomImpl implements MatrixRoom {
	private final MatrixClient client;
	private final MxRoomId roomId;
	private final BehaviorSubject<JoinState> joinState = BehaviorSubject.create();
	private final Map<StateEventKey<?>, BehaviorSubject<?>> roomState = new HashMap<>();
	private final ReplaySubject<RoomEvent> timeline = ReplaySubject.create();

	public Observable<RoomEvent> timeline() {
		return timeline.hide();
	}

	public void receiveTimeline(List<ClientEventWithoutRoomId> incomingTimeline) {
		for (var event : incomingTimeline) {
			var content = EventTypes.getContent(event.type(), event.content());
			timeline.onNext(
				content instanceof Message message
					? new MessageEventImpl(
					this,
					event.eventId(),
					event.sender(),
					event.type(),
					event.content(),
					message)
					: new RoomEventImpl(
					this,
					event.eventId(),
					event.sender(),
					event.type(),
					event.content(),
					content));
		}
	}

	record StateEventKey<T extends EventContent<T>>(
		EventType<T> stateEventType,
		String stateEventKey
	) {
	}

	public MatrixRoomImpl(
		MatrixClient client,
		MxRoomId roomId
	) {
		this.client = client;

		this.roomId = roomId;
	}

	private <T extends EventContent<T>> BehaviorSubject<T> getStateEvent0(EventType<T> eventType, String stateEventKey) {
		var key = new StateEventKey<T>(
			eventType,
			stateEventKey
		);
		//noinspection unchecked
		return (BehaviorSubject<T>) roomState.computeIfAbsent(key, it -> BehaviorSubject.create());
	}

	public <T extends EventContent<T>> Observable<T> observeStateEvent(StateEventType<T> stateEventType, String stateEventKey) {
		return getStateEvent0(stateEventType, stateEventKey).hide();
	}

	public <T extends EventContent<T>> @Nullable T getStateEvent(StateEventType<T> stateEventType, String stateEventKey) {
		return getStateEvent0(stateEventType, stateEventKey).getValue();
	}


	private <T extends EventContent<T>> void setStateEvent0(EventContent<T> eventContent, String stateEventKey) {
		getStateEvent0(eventContent.type(), stateEventKey).onNext(eventContent.asSelf());
	}

	public void setStateEvents(List<ClientEventWithoutRoomId> events) {
		for (var event : events) {
			var content = EventTypes.getContent(event.type(), event.content());
			if (content == null) continue;
			this.setStateEvent0(content, event.stateKey());
		}
	}

	public void setJoinState(JoinState joinState) {
		this.joinState.onNext(joinState);
	}

	@Override
	public MxRoomId roomId() {
		return roomId;
	}

	@Override
	public MatrixClient matrixClient() {
		return client;
	}

	@Override
	public JoinState joinState() {
		return joinState.getValue();
	}

	@Override
	public Observable<JoinState> observeJoinState() {
		return joinState.hide().distinctUntilChanged();
	}
}
