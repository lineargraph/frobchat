package moe.nea.frobmatrix.http;

import moe.nea.frobmatrix.api.UriParameter;

public record IntegerUriParameter(int value) implements UriParameter {
	@Override
	public String asUriParameter() {
		return value + "";
	}
}
