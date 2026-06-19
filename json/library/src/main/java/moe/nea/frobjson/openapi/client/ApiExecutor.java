package moe.nea.frobjson.openapi.client;

import moe.nea.frobjson.openapi.Operation;

import java.util.concurrent.CompletableFuture;

public interface ApiExecutor {
	<P extends Operation.Parameters, B extends Operation.Body, R> CompletableFuture<R> executeOperation(Operation<P, B, R> operation, P parameters, B body);
}
