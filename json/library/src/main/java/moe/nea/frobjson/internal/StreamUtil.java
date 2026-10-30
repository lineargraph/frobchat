package moe.nea.frobjson.internal;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtil {
	public static <T> Stream<T> fromIterable(Iterable<T> iterable) {
		return StreamSupport.stream(iterable.spliterator(), false);
	}

	public record WithIndex<T>(
		T value, int index
	) {
	}

	public static <T> Function<T, WithIndex<T>> withIndex() {
		var index = new AtomicInteger();
		return t -> new WithIndex<>(t, index.getAndIncrement());
	}

	public static <T> Iterable<T> iterable(Stream<T> stream) {
		assert !stream.isParallel();
		AtomicBoolean consumable = new AtomicBoolean(true);
		return () -> {
			assert consumable.getAndSet(false) : "stream consumed twice";
			return stream.iterator();
		};
	}
}
