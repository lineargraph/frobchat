package moe.nea.frobmatrix.types.room;

import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.core.Observable;
import moe.nea.frobmatrix.MxRoomId;
import moe.nea.frobmatrix.event.EventContent;
import moe.nea.frobmatrix.event.StateEventType;
import moe.nea.frobmatrix.types.KeyedStateEventMap;
import moe.nea.frobmatrix.types.MatrixObject;
import moe.nea.frobmatrix.types.event.RoomEvent;
import org.jspecify.annotations.NullMarked;

import java.util.Comparator;
import java.util.Optional;

@NullMarked
public interface MatrixRoom extends MatrixObject {
	Comparator<MatrixRoom> ROOM_ID_COMPARATOR = Comparator.comparing(MatrixRoom::roomId, MxRoomId.COMPARATOR);

	KeyedStateEventMap roomState();

	@Nullable
	JoinState joinState();

	Observable<RoomEvent> timeline();

	Observable<JoinState> observeJoinState();


	Observable<Optional<String>> roomName();

	Observable<String> roomTitle();

	MxRoomId roomId();

}
