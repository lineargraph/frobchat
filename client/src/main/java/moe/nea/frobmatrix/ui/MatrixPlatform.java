package moe.nea.frobmatrix.ui;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import javafx.beans.binding.Binding;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import moe.nea.frobmatrix.MatrixClient;
import moe.nea.frobmatrix.MxMxcUri;
import moe.nea.frobmatrix.MxUserId;
import moe.nea.frobmatrix.api.Authentication;
import moe.nea.frobmatrix.api.MatrixRESTClient;
import moe.nea.frobmatrix.storage.MatrixStorage;
import moe.nea.frobmatrix.storage.MemoryStorage;
import org.pdfsam.rxjavafx.observers.JavaFxObserver;
import org.pdfsam.rxjavafx.schedulers.JavaFxScheduler;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Optional;

public class MatrixPlatform {
	public static MatrixPlatform matrixPlatform;

	public final MatrixClient matrixClient;

	public MatrixPlatform(MatrixClient matrixClient) {
		this.matrixClient = matrixClient;
	}

	public MatrixClient client() {
		return matrixClient;
	}

	public static MatrixPlatform current() {
		return matrixPlatform;
	}

	public <T> Binding<T> asBinding(Observable<T> observable) {
		return JavaFxObserver.toBinding(observable.distinctUntilChanged().observeOn(JavaFxScheduler.platform()));
	}

	public <T> Binding<T> asNullableBinding(Observable<Optional<T>> observable) {
		return JavaFxObserver.toNullableBinding(observable.distinctUntilChanged().observeOn(JavaFxScheduler.platform()));
	}

	public ObservableValue<Image> getAvatar(Observable<Optional<MxMxcUri>> imageUrl) {
		return asNullableBinding(imageUrl
			.map(opt -> opt.map(matrixClient::downloadFile))
			.flatMap(MatrixPlatform::flipOptional))
			.map(it -> new Image(it.toString()));
	}

	private static <T> Maybe<T> fromSingleOptional(Optional<Single<T>> optionalTSingle) {
		return optionalTSingle.map(Maybe::fromSingle).orElse(Maybe.empty());
	}

	private static <T> Observable<Optional<T>> flipOptional(Optional<Single<T>> optionalSingle) {

		if (optionalSingle.isEmpty())
			return Observable.just(Optional.empty());
		return Observable.concat(
			Observable.just(Optional.empty()),
			optionalSingle.get().toObservable().map(Optional::of));
	}

	public static MatrixPlatform login() {
		var TEST_TOKEN = "syt_dGVzdA_oUuqBZxodLPeyCoEyTLR_22RpPL";
		var TEST_USER = MxUserId.parse("@test:nea.moe");
		var TEST_HOMESERVER = "matrix.nea.moe";
		var restClient = new MatrixRESTClient(HttpClient.newHttpClient(), TEST_HOMESERVER, Authentication.token(TEST_TOKEN));
		var storageFile = Path.of("build/statedump");
		var storageBackend = MemoryStorage.tryLoad(storageFile);
		var storage = new MatrixStorage(storageBackend);
		var matrixClient = new MatrixClient(storage, restClient);
		matrixPlatform = new MatrixPlatform(matrixClient);
		matrixClient.start();
		matrixClient.observeStatus()
			.filter(it -> it == MatrixClient.Status.STOPPED)
			.forEach(it -> storageBackend.save(storageFile));
		matrixClient.blockForStatus(MatrixClient.Status.STARTED);
		return matrixPlatform;
	}

}
