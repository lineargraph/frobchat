package moe.nea.frobchat.matrixapi;

import java.net.http.HttpRequest;

public interface MatrixAuthentication {
	void applyTo(HttpRequest.Builder request);

	static MatrixAuthentication anon() {
		return _ -> {
		};
	}
}
