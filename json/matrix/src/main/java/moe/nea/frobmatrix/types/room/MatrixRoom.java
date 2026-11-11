package moe.nea.frobmatrix.types.room;

import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.core.Observable;
import moe.nea.frobmatrix.MxRoomId;
import moe.nea.frobmatrix.event.EventContent;
import moe.nea.frobmatrix.event.StateEventType;
import moe.nea.frobmatrix.types.MatrixObject;
import moe.nea.frobmatrix.types.event.RoomEvent;
import org.jspecify.annotations.NullMarked;

import java.util.Comparator;
import java.util.Optional;

@NullMarked
public interface MatrixRoom extends MatrixObject {
	Comparator<MatrixRoom> ROOM_ID_COMPARATOR = Comparator.comparing(MatrixRoom::roomId, MxRoomId.COMPARATOR);

	@Nullable
	JoinState joinState();

	Observable<RoomEvent> timeline();

	Observable<JoinState> observeJoinState();

	<T extends EventContent<T>> Optional<T> getStateEvent(StateEventType<T> stateEventType, String stateEventKey);

	<T extends EventContent<T>> Observable<Optional<T>> observeStateEvent(StateEventType<T> stateEventType, String stateEventKey);

	default <T extends EventContent<T>> Observable<Optional<T>> observeStateEvent(StateEventType<T> stateEventType) {
		return observeStateEvent(stateEventType, "");
	}

	Observable<Optional<String>> roomName();

	Observable<String> roomTitle();

	MxRoomId roomId();

}
