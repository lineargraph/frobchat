package moe.nea.frobmatrix.api;

import java.net.http.HttpRequest;

public interface Authentication {
	static Authentication token(String token) {
		return new TokenAuthentication(token);
	}
	static Authentication none() {
		return new AnonymousAuthentication();
	}

	void applyTo(HttpRequest.Builder request);
}
