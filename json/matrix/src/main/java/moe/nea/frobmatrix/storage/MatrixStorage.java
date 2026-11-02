package moe.nea.frobmatrix.storage;

import moe.nea.frobmatrix.MxRoomId;
import moe.nea.frobmatrix.api.ResponseHelpers;
import moe.nea.frobmatrix.api.clientserver.definitions.clienteventwithoutroomid.ClientEventWithoutRoomId;
import moe.nea.frobmatrix.util.Pair;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.StringNode;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MatrixStorage {
	private final StorageBackend backend;

	public MatrixStorage(StorageBackend backend) {
		this.backend = backend;
	}

	public void setLastWatermark(String lastWatermark) {
		backend.storeSingleValue("lastWatermark", new StringNode(lastWatermark));
	}

	public String getLastWatermark() {
		return Optional.ofNullable(backend.getSingleValue("lastWatermark"))
			.map(JsonNode::asString)
			.orElse(null);
	}

	public void saveRoomState(MxRoomId roomId, List<ClientEventWithoutRoomId> events) {
		for (ClientEventWithoutRoomId event : events) {
			backend.storeRoomState(roomId.opaqueId(), new Pair<>(event.type(), event.stateKey()), ResponseHelpers.toJsonValue(event));
		}
	}

	public List<ClientEventWithoutRoomId> getRoomState(MxRoomId roomId) {
		var roomState = backend.getRoomState(roomId.opaqueId());
		if (roomState == null) return List.of();
		return roomState.values().stream()
			.map(it -> ResponseHelpers.fromJsonNode(it, ClientEventWithoutRoomId.class))
			.toList();
	}
}
