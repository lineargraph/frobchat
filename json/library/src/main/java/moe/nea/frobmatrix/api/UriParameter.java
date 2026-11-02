package moe.nea.frobmatrix.api;

public interface UriParameter {
	String asUriParameter();

	default boolean isUnitUriParameter() {
		return false;
	}
}
