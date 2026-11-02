package moe.nea.frobmatrix.types.event;

import moe.nea.frobmatrix.event.types.Message;

public interface MessageEvent extends RoomEvent {
	@Override
	Message eventContent();
}
