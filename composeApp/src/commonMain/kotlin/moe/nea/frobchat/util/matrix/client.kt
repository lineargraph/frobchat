package moe.nea.frobchat.util.matrix

import moe.nea.frobchat.matrixapi.MatrixAuthentication
import moe.nea.frobchat.matrixapi.MatrixClient
import moe.nea.frobjson.openapi.Operation
import kotlinx.coroutines.future.await

expect fun createMatrixThinClient(authProviderData: MatrixAuthentication): MatrixClient

suspend fun <P : Operation.Parameters, R : Any> MatrixClient.execute(op: Operation<P, Operation.EmptyBody, R>, p: P): Result<R> {
	return executeOperation(op, p, Operation.EMPTY_BODY).runCatching {
		await().body()
	}
}

