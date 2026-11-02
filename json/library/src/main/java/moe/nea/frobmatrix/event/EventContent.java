package moe.nea.frobmatrix.event;

public interface EventContent<T extends EventContent<T>> {
	EventType<T> type();

	default T asSelf() {
		//noinspection unchecked
		return (T) this;
	}
}
