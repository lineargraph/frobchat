package moe.nea.frobmatrix.storage;

import moe.nea.frobmatrix.MxRoomId;
import moe.nea.frobmatrix.api.clientserver.definitions.clienteventwithoutroomid.ClientEventWithoutRoomId;

import java.util.List;

public class MatrixStorage {
	private final StorageBackend backend;

	public MatrixStorage(StorageBackend backend) {
		this.backend = backend;
	}

	String lastWatermark;

	public void setLastWatermark(String lastWatermark) {
		this.lastWatermark = lastWatermark;
	}

	public String getLastWatermark() {
		return lastWatermark;
	}

	public void saveRoomState(MxRoomId roomId, List<ClientEventWithoutRoomId> events) {
	}

	public void getRoomState(MxRoomId roomId) {
	}
}
