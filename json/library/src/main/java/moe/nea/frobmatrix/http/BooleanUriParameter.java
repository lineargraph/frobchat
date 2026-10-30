package moe.nea.frobmatrix.http;

import moe.nea.frobmatrix.api.UriParameter;

public record BooleanUriParameter(boolean value) implements UriParameter {
	@Override
	public String asUriParameter() {
		return value + "";
	}
}
