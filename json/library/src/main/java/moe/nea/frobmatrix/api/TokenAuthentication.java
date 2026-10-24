package moe.nea.frobmatrix.api;

import java.net.http.HttpRequest;

public record TokenAuthentication(
	String token
) implements Authentication {
	@Override
	public void applyTo(HttpRequest.Builder request) {
		request.header("Authorization", "Bearer " + token);
	}
}
