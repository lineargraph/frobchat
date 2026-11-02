package moe.nea.frobmatrix.types.event;

import moe.nea.frobmatrix.MxEventId;
import moe.nea.frobmatrix.MxUserId;
import moe.nea.frobmatrix.event.EventContent;
import moe.nea.frobmatrix.event.RawEventContent;
import moe.nea.frobmatrix.types.room.MatrixRoom;
import org.jspecify.annotations.Nullable;

public class RoomEventImpl implements RoomEvent {
	private final MatrixRoom room;
	private final MxEventId eventId;
	private final MxUserId senderId;
	private final RawEventContent rawEventContent;
	private final String type;
	private final EventContent<?> eventContent;

	public String type() {
		return type;
	}

	@Override
	public MatrixRoom room() {
		return room;
	}

	@Override
	public @Nullable EventContent<?> eventContent() {
		return eventContent;
	}

	@Override
	public RawEventContent rawEventContent() {
		return rawEventContent;
	}

	@Override
	public MxEventId eventId() {
		return eventId;
	}

	@Override
	public MxUserId senderId() {
		return senderId;
	}

	public RoomEventImpl(
		MatrixRoom room,
		MxEventId eventId,
		MxUserId senderId,
		String type,
		RawEventContent rawEventContent,
		@Nullable EventContent<?> eventContent
	) {
		this.room = room;
		this.eventId = eventId;
		this.senderId = senderId;
		this.type = type;
		this.rawEventContent = rawEventContent;
		this.eventContent = eventContent;
	}
}
