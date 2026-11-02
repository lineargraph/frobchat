package moe.nea.frobmatrix;

import io.reactivex.rxjava3.core.Flowable;
import moe.nea.frobmatrix.api.MatrixRESTClient;
import moe.nea.frobmatrix.api.clientserver.routes.sync.Sync;
import moe.nea.frobmatrix.storage.MatrixStorage;
import moe.nea.frobmatrix.types.room.JoinState;
import moe.nea.frobmatrix.types.room.MatrixRoom;
import moe.nea.frobmatrix.types.room.MatrixRoomImpl;

import java.util.HashMap;
import java.util.Map;

public class MatrixClient {
	final MatrixStorage storage;
	final MatrixRESTClient restClient;
	final Map<MxRoomId, MatrixRoom> rooms = new HashMap<>();

	public MatrixStorage getStorage() {
		return storage;
	}

	public MatrixRESTClient getRestClient() {
		return restClient;
	}

	public MatrixClient(
		MatrixStorage storage,
		MatrixRESTClient restClient
	) {
		this.storage = storage;
		this.restClient = restClient;
	}

	public MatrixRoom getRoom(MxRoomId roomId) {
		return rooms.computeIfAbsent(roomId, roomId1 -> {
			var oldRoomState = this.storage.getRoomState(roomId1);
			var roomImpl = new MatrixRoomImpl(this, roomId1);
			roomImpl.setStateEvents(oldRoomState);
			return roomImpl;
		});
	}

	public void syncOnce() {
		var sync = getRestClient().execute(new Sync(
			null,
			storage.getLastWatermark(),
			true,
			null,
			1000,
			true
		));
		storage.setLastWatermark(sync.nextBatch());
		for (var roomEntry : sync.rooms().join().additionalProperties().entrySet()) {
			var roomId = roomEntry.getKey();
			var joinedRoom = roomEntry.getValue();
			var room = (MatrixRoomImpl) getRoom(roomId);
			room.setJoinState(JoinState.JOINED);
			if (joinedRoom.stateAfter() != null) {
				storage.saveRoomState(roomId, joinedRoom.stateAfter().events());
				room.setStateEvents(joinedRoom.stateAfter().events());
			}
			if (joinedRoom.state() != null) {
				storage.saveRoomState(roomId, joinedRoom.state().events());
				room.setStateEvents(joinedRoom.state().events());
				var timelineStateEvents =
					joinedRoom.timeline().events()
						.stream()
						.filter(it -> it.stateKey() != null)
						.toList();
				storage.saveRoomState(roomId, timelineStateEvents);
				room.setStateEvents(timelineStateEvents);
			}
		}
		for (var roomEntry : sync.rooms().join().additionalProperties().entrySet()) {
			var roomId = roomEntry.getKey();
			var joinedRoom = roomEntry.getValue();
			var room = (MatrixRoomImpl) getRoom(roomId);
			room.receiveTimeline(joinedRoom.timeline().events());
		}
	}
}
