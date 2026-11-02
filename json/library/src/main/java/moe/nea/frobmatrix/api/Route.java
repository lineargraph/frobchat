package moe.nea.frobmatrix.api;

import moe.nea.frobmatrix.http.UriBuilder;
import org.jspecify.annotations.Nullable;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Supplier;

public abstract class Route<T> {
	protected abstract HttpResponse.BodySubscriber<Supplier<T>> handleResponseInfo(HttpResponse.ResponseInfo responseInfo);

	public abstract Method method();

	public abstract UriBuilder appendUriTo(UriBuilder uriBuilder);

	public abstract String rawPath();

	public enum Method {
		GET, POST, PUT
	}

	public HttpResponse<Supplier<T>> execute(MatrixRESTClient client) {
		try {
			return client.client().send(asHttpRequest(client), bodyHandler());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public HttpRequest asHttpRequest(MatrixRESTClient client) {
		return client.postProcess(buildHttpRequest(client)).build();
	}

	public HttpRequest.Builder buildHttpRequest(MatrixRESTClient client) {
		var builder = HttpRequest.newBuilder(appendUriTo(client.createUriBuilder()).build())
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

	public HttpResponse.BodyHandler<Supplier<T>> bodyHandler() {
		return this::handleResponseInfo;
	}

}
