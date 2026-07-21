package moe.nea.frobchat.matrixapi;

import java.net.http.HttpRequest;

public interface MatrixAuthentication {
	String baseUrl();

	void applyTo(HttpRequest.Builder request);

	default MatrixAuthentication withToken(String token) {
		return token(baseUrl(), token);
	}

	record Anon(String baseUrl) implements MatrixAuthentication {
		@Override
		public void applyTo(HttpRequest.Builder request) {
		}
	}

	record AuthToken(String baseUrl, String token) implements MatrixAuthentication {
		@Override
		public void applyTo(HttpRequest.Builder request) {
			request.header("authorization", "Bearer " + token);
		}
	}

	static MatrixAuthentication token(String baseUrl, String token) {
		return new AuthToken(baseUrl, token);
	}

	static MatrixAuthentication anon(String baseUrl) {
		return new Anon(baseUrl);
	}
}
