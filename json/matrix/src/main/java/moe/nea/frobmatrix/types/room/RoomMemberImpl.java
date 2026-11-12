package moe.nea.frobmatrix.types.room;

import io.reactivex.rxjava3.core.Observable;
import moe.nea.frobmatrix.MatrixClient;
import moe.nea.frobmatrix.MxUserId;
import moe.nea.frobmatrix.event.types.TheCurrentMembershipStateOfAUserInTheRoom;

import java.util.Optional;

public class RoomMemberImpl implements RoomMember {
	private final MatrixRoomImpl room;
	private final MxUserId userId;
	private final Observable<Optional<TheCurrentMembershipStateOfAUserInTheRoom>> membership;

	public RoomMemberImpl(MatrixRoomImpl room, MxUserId userId) {
		this.room = room;
		this.userId = userId;
		this.membership = room.roomState().observeStateEvent(TheCurrentMembershipStateOfAUserInTheRoom.TYPE);
	}

	@Override public Observable<Optional<String>> name() {
		return membership.map(opt -> opt.map(TheCurrentMembershipStateOfAUserInTheRoom::displayname));
	}

	@Override public Observable<String> title() {
		return name().map(it -> it.orElse(userId.name()));
	}

	@Override public MatrixClient matrixClient() {
		return room.matrixClient();
	}

	@Override public MxUserId userId() {
		return userId;
	}

}
