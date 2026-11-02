package moe.nea.frobmatrix.types.event;

import moe.nea.frobmatrix.MatrixClient;
import moe.nea.frobmatrix.MxEventId;
import moe.nea.frobmatrix.MxUserId;
import moe.nea.frobmatrix.event.EventContent;
import moe.nea.frobmatrix.event.RawEventContent;
import moe.nea.frobmatrix.types.MatrixObject;
import moe.nea.frobmatrix.types.room.MatrixRoom;

public interface RoomEvent extends MatrixObject {

	@Override
	default MatrixClient matrixClient() {
		return room().matrixClient();
	}

	MatrixRoom room();

	EventContent<?> eventContent();

	RawEventContent rawEventContent();

	MxEventId eventId();

	MxUserId senderId();
}
