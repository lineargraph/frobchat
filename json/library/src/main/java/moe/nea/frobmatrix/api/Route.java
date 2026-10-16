package moe.nea.frobmatrix.api;

public abstract class Route {
	public enum Method {
		GET, POST
	}

	public abstract Method method();

	public abstract String rawPath();
}
