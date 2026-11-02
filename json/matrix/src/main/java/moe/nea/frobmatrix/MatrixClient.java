package moe.nea.frobmatrix;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;
import moe.nea.frobmatrix.api.MatrixRESTClient;
import moe.nea.frobmatrix.api.ResponseHelpers;
import moe.nea.frobmatrix.api.clientserver.routes.accountdata.GetAccountData;
import moe.nea.frobmatrix.api.clientserver.routes.profile.DeleteProfileField;
import moe.nea.frobmatrix.api.clientserver.routes.profile.GetUserProfile;
import moe.nea.frobmatrix.api.clientserver.routes.profile.SetProfileField;
import moe.nea.frobmatrix.api.clientserver.routes.sync.Sync;
import moe.nea.frobmatrix.api.clientserver.routes.whoami.GetTokenOwner;
import moe.nea.frobmatrix.storage.MatrixStorage;
import moe.nea.frobmatrix.types.LogInInfo;
import moe.nea.frobmatrix.types.room.JoinState;
import moe.nea.frobmatrix.types.room.MatrixRoom;
import moe.nea.frobmatrix.types.room.MatrixRoomImpl;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.StringNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MatrixClient {
	private static final Logger log = LoggerFactory.getLogger(MatrixClient.class);
	final MatrixStorage storage;
	final MatrixRESTClient restClient;
	final BehaviorSubject<Map<MxRoomId, MatrixRoom>> rooms = BehaviorSubject.createDefault(Map.of());

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

	public Observable<Map<MxRoomId, MatrixRoom>> observeRooms() {
		return rooms.hide();
	}

	public MatrixRoom getRoom(MxRoomId roomId) {
		var current = rooms.getValue();
		var existing = current.get(roomId);
		if (existing != null) return existing;

		var oldRoomState = this.storage.getRoomState(roomId);
		var roomImpl = new MatrixRoomImpl(this, roomId);
		roomImpl.setStateEvents(oldRoomState);
		var next = new LinkedHashMap<>(current);
		next.put(roomId, roomImpl);
		rooms.onNext(next);
		return roomImpl;
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

	private Thread syncThread;

	private AtomicBoolean stopped = new AtomicBoolean(false);

	public enum Status {
		STARTING,
		STARTED,
		STOPPED
	}

	private final PublishSubject<Status> onStatus = PublishSubject.create();

	public Observable<Status> observeStatus() {
		return onStatus.hide();
	}

	public void shutdown() {
		stopped.setRelease(true);
	}

	private void syncLoop() {
		onStatus.onNext(Status.STARTING);
		verifyLogin();
		onStatus.onNext(Status.STARTED);
		while (!stopped.getAcquire()) {
			syncOnce();
		}
		onStatus.onNext(Status.STOPPED);
		onStatus.onComplete();
	}

	private BehaviorSubject<LogInInfo> logInInfo = BehaviorSubject.create();
	private BehaviorSubject<GetUserProfile.Response> userProfile = BehaviorSubject.create();

	public void refreshUserProfile() {
		var userProfile = restClient.execute(new GetUserProfile(getLogInInfo().userId()));
		log.info("Received user profile: {}", userProfile.displayname());
		this.userProfile.onNext(userProfile);
	}

	public Observable<String> getDisplayName() {
		return userProfile.map(GetUserProfile.Response::displayname);
	}

	public Observable<MxMxcUri> getAvatarUri() {
		return userProfile.map(GetUserProfile.Response::avatarUrl);
	}

	public void setProfileField(String key, JsonNode value) {
		restClient.execute(new SetProfileField(Map.of(key, value), getLogInInfo().userId(), key));
	}

	public void deleteProfileField(String key) {
		restClient.execute(new DeleteProfileField(getLogInInfo().userId(), key));
	}

	public void setAvatarUri(MxMxcUri avatarUri) {
		setProfileField("avatarurl", ResponseHelpers.toJsonValue(avatarUri));
	}

	public LogInInfo getLogInInfo() {
		return Objects.requireNonNull(logInInfo.getValue(), "Not logged in yet");
	}

	private void verifyLogin() {
		var tokenOwner = restClient.execute(new GetTokenOwner());
		logInInfo.onNext(new LogInInfo(tokenOwner.userId(), tokenOwner.deviceId(), tokenOwner.isGuest()));
		refreshUserProfile();
	}


	public void start() {
		if (syncThread != null)
			throw new RuntimeException("Trying to start already started MatrixClient");
		syncThread = new Thread(this::syncLoop, "MatrixClient-Sync");
		syncThread.start();
	}
}
