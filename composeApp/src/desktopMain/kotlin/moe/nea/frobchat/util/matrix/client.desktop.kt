package moe.nea.frobchat.util.matrix

import java.net.http.HttpClient
import moe.nea.frobchat.matrixapi.MatrixAuthentication
import moe.nea.frobchat.matrixapi.MatrixClient

val httpClient = HttpClient.newBuilder()
	.build()

actual fun createMatrixThinClient(authProviderData: MatrixAuthentication): MatrixClient {
	return MatrixClient(authProviderData.baseUrl(), httpClient, authProviderData)
}
