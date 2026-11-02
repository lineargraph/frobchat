import io.reactivex.rxjava3.core.Flowable;
import moe.nea.frobmatrix.MatrixClient;
import moe.nea.frobmatrix.MxRoomId;
import moe.nea.frobmatrix.MxUserId;
import moe.nea.frobmatrix.api.Authentication;
import moe.nea.frobmatrix.api.MatrixRESTClient;
import moe.nea.frobmatrix.api.ResponseHelpers;
import moe.nea.frobmatrix.api.clientserver.ClientServerApi;
import moe.nea.frobmatrix.api.clientserver.routes.sync.Sync;
import moe.nea.frobmatrix.api.clientserver.routes.sync.SyncApi;
import moe.nea.frobmatrix.api.clientserver.routes.wellknown.GetWellknown;
import moe.nea.frobmatrix.api.clientserver.routes.whoami.GetTokenOwner;
import moe.nea.frobmatrix.storage.MatrixStorage;
import moe.nea.frobmatrix.storage.MemoryStorage;
import moe.nea.frobmatrix.types.event.MessageEvent;

import java.io.File;
import java.io.ObjectInputStream;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
	public static void main(String[] args) {
		var TEST_TOKEN = "syt_dGVzdA_oUuqBZxodLPeyCoEyTLR_22RpPL";
		var TEST_USER = MxUserId.parse("@test:nea.moe");
		var TEST_HOMESERVER = "matrix.nea.moe";
		var restClient = new MatrixRESTClient(HttpClient.newHttpClient(), "matrix.nea.moe", Authentication.token(TEST_TOKEN));
		var storageFile = Path.of("build/statedump");
		var storageBackend = MemoryStorage.tryLoad(storageFile);
		var storage = new MatrixStorage(storageBackend);
		var client = new MatrixClient(storage, restClient);
		var dmsWithMe = client.getRoom(new MxRoomId("IVeSfvhpVNHxhhhtKk:nea.moe"));
		AtomicBoolean shutdownSignal = new AtomicBoolean(false);
		var disposable = dmsWithMe.timeline()
			.ofType(MessageEvent.class)
			.forEach(it -> {
				System.out.println(it.eventContent().body());
				if (it.eventContent().body().contains("stop"))
					shutdownSignal.set(true);
			});
		while (!shutdownSignal.get())
			client.syncOnce();
		disposable.dispose();
		storageBackend.save(storageFile);
	}
}
