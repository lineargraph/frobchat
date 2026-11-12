package moe.nea.frobmatrix.types.room;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import moe.nea.frobmatrix.MatrixClient;
import moe.nea.frobmatrix.MxRoomId;
import moe.nea.frobmatrix.MxUserId;
import moe.nea.frobmatrix.api.clientserver.definitions.clienteventwithoutroomid.ClientEventWithoutRoomId;
import moe.nea.frobmatrix.event.EventTypes;
import moe.nea.frobmatrix.event.ParsedEventContent;
import moe.nea.frobmatrix.event.types.Message;
import moe.nea.frobmatrix.event.types.RoomName;
import moe.nea.frobmatrix.event.types.TheCurrentMembershipStateOfAUserInTheRoom;
import moe.nea.frobmatrix.types.KeyedStateEventMap;
import moe.nea.frobmatrix.types.StateEventMapImpl;
import moe.nea.frobmatrix.types.event.MessageEventImpl;
import moe.nea.frobmatrix.types.event.RoomEvent;
import moe.nea.frobmatrix.types.event.RoomEventImpl;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@NullMarked
public class MatrixRoomImpl implements MatrixRoom {
	private final MatrixClient client;
	private final MxRoomId roomId;
	private final BehaviorSubject<JoinState> joinState = BehaviorSubject.create();
	private final StateEventMapImpl stateEventMap = new StateEventMapImpl();
	private final ReplaySubject<RoomEvent> timeline = ReplaySubject.create();
	private final Observable<Optional<MxUserId>> dmConversationPartner;
	private final Map<MxUserId, RoomMemberImpl> roomMembers = new HashMap<>();

	public MatrixRoomImpl(
		MatrixClient client,
		MxRoomId roomId
	) {
		this.client = client;
		this.roomId = roomId;
		this.dmConversationPartner = matrixClient().getAccountData()
			.directMessages()
			.map(it -> Optional.ofNullable(it.get(roomId())))
			.distinctUntilChanged();
	}

	private RoomMember getRoomMember0(MxUserId userId) {
		return roomMembers.computeIfAbsent(userId, ignored -> new RoomMemberImpl(this, userId));
	}

	@Override public Optional<RoomMember> getRoomMember(MxUserId userId) {
		var membership = roomState().getStateEvent(TheCurrentMembershipStateOfAUserInTheRoom.TYPE);
		if (membership.isEmpty())
			return Optional.empty();
		return Optional.of(getRoomMember0(userId));
	}

	@Override public KeyedStateEventMap roomState() {
		return stateEventMap;
	}

	@Override public Observable<Optional<MxUserId>> dmConversationPartnerId() {
		return dmConversationPartner;
	}

	@Override public Observable<Optional<RoomMember>> dmConversationPartner() {
		return dmConversationPartnerId()
			.map(userId -> userId.map(this::getRoomMember0));
	}

	public Observable<RoomEvent> timeline() {
		return timeline.hide();
	}

	public void receiveTimeline(List<ClientEventWithoutRoomId> incomingTimeline) {
		for (var event : incomingTimeline) {
			EventTypes.parseContent(event.type(), event.content())
				.flatMap(ParsedEventContent::value)
				.ifPresent(content -> {
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
				});
		}
	}

	@Override public Observable<Optional<String>> roomName() {
		return roomState().observeStateEvent(RoomName.TYPE).map(it -> it.map(RoomName::name));
	}

	@Override public Observable<String> roomTitle() {
		return Observable.combineLatest(
			roomName(),
			dmConversationPartner()
				.switchMap(partnerOpt ->
					partnerOpt.isPresent() ? partnerOpt.get().title().map(Optional::of) : Observable.just(Optional.<String>empty())),
			(name, partner) -> {
				if (name.isPresent())
					return name.get();
				if (partner.isPresent())
					return partner.get();
				return roomId().asUriParameter();
			});
	}


	public void setStateEvents(List<ClientEventWithoutRoomId> events) {
		for (var event : events) {
			stateEventMap.setStateEvent(EventTypes.parseContent(event.type(), event.content()), event.stateKey());
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
