package moe.nea.frobmatrix.api;

import java.net.http.HttpRequest;

public class AnonymousAuthentication implements Authentication {
	@Override
	public void applyTo(HttpRequest.Builder request) {
	}
}
