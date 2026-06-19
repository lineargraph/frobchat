package moe.nea.frobchat.matrixapi;

import java.net.http.HttpRequest;

public interface MatrixAuthentication {
	String baseUrl();

	void applyTo(HttpRequest.Builder request);

	record Anon(String baseUrl) implements MatrixAuthentication {
		@Override
		public void applyTo(HttpRequest.Builder request) {
		}
	}

	static MatrixAuthentication anon(String baseUrl) {
		return new Anon(baseUrl);
	}
}
