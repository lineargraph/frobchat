package moe.nea.frobjson.generator.matrix;

import java.util.function.Supplier;

public class Lazy<T> {
	Supplier<T> supplier;
	T value;

	public Lazy(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	public static <T> Lazy<T> of(Supplier<T> supplier) {
		return new Lazy<>(supplier);
	}

	public T get() {
		if (value == null) {
			value = supplier.get();
			supplier = null;
		}
		return value;
	}
}
