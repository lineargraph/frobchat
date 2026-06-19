package moe.nea.frobchat.matrixapi;

import moe.nea.frobchat.matrixapi.operations.Client;
import moe.nea.frobjson.openapi.Operation;
import moe.nea.frobjson.openapi.client.JavaHttpClient;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class MatrixClient extends JavaHttpClient {
	private final MatrixAuthentication authentication;

	public static Client client(HttpClient client, MatrixAuthentication authentication) {
		return new Client(new MatrixClient(client, authentication));
	}

	public MatrixClient(HttpClient client, MatrixAuthentication authentication) {
		super(authentication.baseUrl(), client);
		this.authentication = authentication;
	}

	@Override
	public <P extends Operation.Parameters, B extends Operation.Body, R> HttpRequest.Builder buildRequest(Operation<P, B, R> operation, P parameters, B body) {
		var request = super.buildRequest(operation, parameters, body);
		authentication.applyTo(request);
		return request;
	}
}
