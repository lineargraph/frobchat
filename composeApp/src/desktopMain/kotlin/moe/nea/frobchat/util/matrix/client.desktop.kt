package moe.nea.frobchat.util.matrix

import java.net.http.HttpClient
import moe.nea.frobchat.matrixapi.MatrixAuthentication
import moe.nea.frobchat.matrixapi.MatrixClient
import moe.nea.frobchat.matrixapi.operations.Client

val httpClient = HttpClient.newBuilder()
	.build()

actual fun createMatrixThinClient(authProviderData: MatrixAuthentication): Client {
	return MatrixClient.client(httpClient, authProviderData)
}
