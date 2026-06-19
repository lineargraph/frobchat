package moe.nea.frobjson.openapi.client;

public class ApiClient {
	final ApiExecutor executor;

	public ApiExecutor executor() {
		return executor;
	}

	public ApiClient(ApiExecutor executor) {
		this.executor = executor;
	}
}
