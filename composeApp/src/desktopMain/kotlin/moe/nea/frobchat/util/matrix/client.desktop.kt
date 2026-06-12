package moe.nea.frobchat.util.matrix

import androidx.room.Room.databaseBuilder
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import de.connect2x.trixnity.api.client.MatrixApiClient
import de.connect2x.trixnity.client.CryptoDriverModule
import de.connect2x.trixnity.client.MatrixClient
import de.connect2x.trixnity.client.MediaStoreModule
import de.connect2x.trixnity.client.RepositoriesModule
import de.connect2x.trixnity.client.create
import de.connect2x.trixnity.client.cryptodriver.libolm.libOlm
import de.connect2x.trixnity.client.media.okio.okio
import de.connect2x.trixnity.client.store.repository.room.TrixnityRoomDatabase
import de.connect2x.trixnity.client.store.repository.room.room
import de.connect2x.trixnity.clientserverapi.client.MatrixClientAuthProviderData
import de.connect2x.trixnity.clientserverapi.client.MatrixClientAuthProviderDataStore
import de.connect2x.trixnity.clientserverapi.client.MatrixClientServerApiBaseClient
import de.connect2x.trixnity.clientserverapi.client.MatrixClientServerApiClient
import de.connect2x.trixnity.clientserverapi.client.MatrixClientServerApiClientImpl
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.jetty.Jetty
import moe.nea.frobchat.config.DataStore
import okio.Path.Companion.toPath
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual suspend fun createMatrixClient(authProviderData: MatrixClientAuthProviderData): MatrixClient {
	val cacheDir = DataStore.paths.cacheDir.toPath()
	return MatrixClient.create(
		RepositoriesModule.room(
			databaseBuilder<TrixnityRoomDatabase>(cacheDir.resolve("messages.db").toString())
				.setDriver(BundledSQLiteDriver())
		),
		MediaStoreModule.okio(cacheDir.resolve("media")),
		CryptoDriverModule.libOlm(),
		authProviderData
	) {
		this.httpClientEngine = sharedHttpClient.engine
		this.httpClientConfig = sharedHttpClientConfig
	}.getOrThrow() // TODO: dont throw here
}

actual val sharedHttpClient: HttpClient = HttpClient(Jetty, sharedHttpClientConfig)
actual fun createMatrixThinClient(
	authProviderData: MatrixClientAuthProviderData,
): MatrixClientServerApiClient {
	return MatrixClientServerApiClientImpl(
		authProvider = authProviderData.createAuthProvider(
			store = MatrixClientAuthProviderDataStore.inMemory(authProviderData),
			httpClientEngine = sharedHttpClient.engine,
			httpClientConfig = sharedHttpClientConfig,
		),
		httpClientEngine = sharedHttpClient.engine,
		httpClientConfig = sharedHttpClientConfig,
	)
}
