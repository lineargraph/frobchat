package moe.nea.frobmatrix.ui;

import io.reactivex.rxjava3.core.Observable;
import moe.nea.frobmatrix.MatrixClient;
import moe.nea.frobmatrix.MxRoomId;
import moe.nea.frobmatrix.MxUserId;
import moe.nea.frobmatrix.api.Authentication;
import moe.nea.frobmatrix.api.MatrixRESTClient;
import moe.nea.frobmatrix.storage.MatrixStorage;
import moe.nea.frobmatrix.storage.MemoryStorage;

import java.net.http.HttpClient;
import java.nio.file.Path;

public class MatrixPlatform {
	public static MatrixClient matrixClient;


	public static void login() {
		var TEST_TOKEN = "syt_dGVzdA_oUuqBZxodLPeyCoEyTLR_22RpPL";
		var TEST_USER = MxUserId.parse("@test:nea.moe");
		var TEST_HOMESERVER = "matrix.nea.moe";
		var restClient = new MatrixRESTClient(HttpClient.newHttpClient(), TEST_HOMESERVER, Authentication.token(TEST_TOKEN));
		var storageFile = Path.of("build/statedump");
		var storageBackend = MemoryStorage.tryLoad(storageFile);
		var storage = new MatrixStorage(storageBackend);
		matrixClient = new MatrixClient(storage, restClient);
		matrixClient.start();
		matrixClient.observeStatus()
			.filter(it -> it == MatrixClient.Status.STOPPED)
			.forEach(it -> storageBackend.save(storageFile));
	}

}
