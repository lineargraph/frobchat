package moe.nea.frobmatrix.http;

import moe.nea.frobmatrix.api.UriParameter;

public record StringUriParameter(String value) implements UriParameter {
	@Override
	public String asUriParameter() {
		return value;
	}
}
