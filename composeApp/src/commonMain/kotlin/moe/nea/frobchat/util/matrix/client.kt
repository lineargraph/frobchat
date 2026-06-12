package moe.nea.frobchat.util.matrix

import de.connect2x.trixnity.client.MatrixClient
import de.connect2x.trixnity.clientserverapi.client.MatrixClientAuthProviderData
import de.connect2x.trixnity.clientserverapi.client.MatrixClientServerApiClient
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

val sharedHttpClientConfig: (HttpClientConfig<*>.() -> Unit) = {

}
expect val sharedHttpClient: HttpClient
expect suspend fun createMatrixClient(authProviderData: MatrixClientAuthProviderData): MatrixClient
expect fun createMatrixThinClient(authProviderData: MatrixClientAuthProviderData) : MatrixClientServerApiClient
