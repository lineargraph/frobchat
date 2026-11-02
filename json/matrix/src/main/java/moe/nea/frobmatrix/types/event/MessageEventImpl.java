package moe.nea.frobmatrix.types.event;

import moe.nea.frobmatrix.MxEventId;
import moe.nea.frobmatrix.MxUserId;
import moe.nea.frobmatrix.event.RawEventContent;
import moe.nea.frobmatrix.event.types.Message;
import moe.nea.frobmatrix.types.room.MatrixRoom;

public class MessageEventImpl extends RoomEventImpl implements MessageEvent {

	private final Message message;

	@Override
	public Message eventContent() {
		return message;
	}

	public MessageEventImpl(MatrixRoom room, MxEventId eventId, MxUserId senderId, String type, RawEventContent rawEventContent, Message message) {
		super(room, eventId, senderId, type, rawEventContent, message);
		this.message = message;
	}
}
