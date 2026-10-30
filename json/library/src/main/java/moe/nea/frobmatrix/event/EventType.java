package moe.nea.frobmatrix.event;

public interface EventType<T extends EventContent<T>> {
	String type();

	Class<T> contentType();
}
