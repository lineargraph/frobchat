package moe.nea.frobmatrix.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class Route<T> {
	protected abstract HttpResponse.BodySubscriber<T> handleResponseInfo(HttpResponse.ResponseInfo responseInfo);

	public abstract Method method();

	public abstract String rawPath();

	public enum Method {
		GET, POST
	}

	public HttpResponse<T> execute(HttpClient client) {
		try {
			return client.send(asHttpRequest(), bodyHandler());
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public HttpRequest asHttpRequest() {
		return buildHttpRequest().build();
	}

	public HttpRequest.Builder buildHttpRequest() {
		return HttpRequest.newBuilder(URI.create("https://nea.moe" + rawPath()))
			.method(method().name(), HttpRequest.BodyPublishers.noBody())
			.header("accept", "application/json");
	}

	public HttpResponse.BodyHandler<T> bodyHandler() {
		return this::handleResponseInfo;
	}

}
