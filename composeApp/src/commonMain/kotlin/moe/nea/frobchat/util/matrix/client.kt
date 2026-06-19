package moe.nea.frobchat.util.matrix

import java.util.concurrent.CompletableFuture
import moe.nea.frobchat.matrixapi.MatrixAuthentication
import moe.nea.frobchat.matrixapi.operations.Client
import kotlinx.coroutines.future.await

expect fun createMatrixThinClient(authProviderData: MatrixAuthentication): Client

suspend fun <T> CompletableFuture<T>.awaitResult() = runCatching { await() }
