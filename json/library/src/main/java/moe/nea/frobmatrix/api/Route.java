package moe.nea.frobmatrix.api;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class Route<T> {
	protected abstract HttpResponse.BodySubscriber<T> handleResponseInfo(HttpResponse.ResponseInfo responseInfo);

	public abstract Method method();

	public abstract String rawPath();

	public abstract String path();

	public enum Method {
		GET, POST, PUT
	}

	public HttpResponse<T> execute(MatrixRESTClient client) {
		try {
			return client.client().send(asHttpRequest(client), bodyHandler());
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public HttpRequest asHttpRequest(MatrixRESTClient client) {
		return client.postProcess(buildHttpRequest(client)).build();
	}

	public HttpRequest.Builder buildHttpRequest(MatrixRESTClient client) {
		var builder = HttpRequest.newBuilder(URI.create(client.basePath() + path()))
			.method(method().name(), bodyPublisher())
			.header("accept", "application/json");
		var contentType = contentType();
		if (contentType != null)
			builder.header("Content-Type", contentType);
		return builder;
	}

	public @Nullable String contentType() {
		return null;
	}

	public HttpRequest.BodyPublisher bodyPublisher() {
		return HttpRequest.BodyPublishers.noBody();
	}

	public HttpResponse.BodyHandler<T> bodyHandler() {
		return this::handleResponseInfo;
	}

}
