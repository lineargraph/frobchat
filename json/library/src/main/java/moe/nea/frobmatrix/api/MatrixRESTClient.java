package moe.nea.frobmatrix.api;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class MatrixRESTClient {
	private Authentication authentication;
	private HttpClient client;
	private String homeServer;

	public MatrixRESTClient(
		HttpClient client,
		String homeServer,
		Authentication authentication) {
		this.client = client;
		this.homeServer = homeServer;
		this.authentication = authentication;
	}

	public Authentication authentication() {
		return authentication;
	}

	public HttpClient client() {
		return client;
	}

	public String homeServer() {
		return homeServer;
	}

	public String basePath() {
		return "https://" + homeServer();
	}

	public HttpRequest.Builder postProcess(HttpRequest.Builder builder) {
		authentication.applyTo(builder);
		return builder;
	}
}
