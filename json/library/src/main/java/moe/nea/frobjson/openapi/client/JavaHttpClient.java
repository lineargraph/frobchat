package moe.nea.frobjson.openapi.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import moe.nea.frobjson.openapi.Operation;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class JavaHttpClient implements ApiExecutor {
	private final String baseUrl;
	private final HttpClient client;

	public JavaHttpClient(
		String baseUrl,
		HttpClient client) {
		this.baseUrl = baseUrl;
		this.client = client;
	}

	public <P extends Operation.Parameters, B extends Operation.Body, R> CompletableFuture<R> executeOperation(
		Operation<P, B, R> operation,
		P parameters,
		B body
	) {
		return client.sendAsync(buildRequest(operation, parameters, body).build(), bodyHandler(operation)).thenApply(HttpResponse::body);
	}

	public <R> HttpResponse.BodyHandler<R> bodyHandler(Operation<?, ?, R> operation) {
		return responseInfo -> {
			var status = responseInfo.statusCode();
			// TODO: for now we just always parse JSON. but we could also add other handlers here, switching on status / content-type
			return HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8),
				inputStream -> {
					// TODO: lol utf8
					JsonElement json;
					try {
						json = new Gson().fromJson(inputStream, JsonElement.class);
					} catch (JsonSyntaxException ex) {
						throw new RequestParsingException("Could not parse JSON", inputStream, ex);
					}
					try {
						return operation.fromResponse(status, json);
					} catch (Exception e) {
						throw new RequestParsingException("Could not unmarshal JSON for status " + status, inputStream, e);
					}
				});
		};
	}


	public <P extends Operation.Parameters, B extends Operation.Body, R> HttpRequest.Builder buildRequest(
		Operation<P, B, R> operation,
		P parameters,
		B body
	) {
		return HttpRequest.newBuilder()
			.method(operation.method().toUpperCase(Locale.ROOT), publisherFor(body))
			.uri(URI.create(baseUrl + operation.path(parameters) + parameters.queryParameters().asQueryString()));
	}

	public static String urlEncode(String raw) {
		return URLEncoder.encode(raw, StandardCharsets.UTF_8);
	}

	private <B extends Operation.Body> HttpRequest.BodyPublisher publisherFor(B body) {
		switch (body) {
			case Operation.EmptyBody ignored -> {
				return HttpRequest.BodyPublishers.noBody();
			}
			case Operation.JsonBody<?> jsonBody -> {
				var json = jsonBody.asJson();
				if (json == null)
					return HttpRequest.BodyPublishers.noBody();
				return HttpRequest.BodyPublishers.ofString(json.toString());
			}
		}
	}

}
