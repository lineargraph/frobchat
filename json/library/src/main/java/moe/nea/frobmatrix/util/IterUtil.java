package moe.nea.frobmatrix.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class IterUtil {

	public static <T> Iterable<T> wrapOnceIterator(Iterator<T> iterator) {
		return new Iterable<>() {
			boolean iteratorUsed = false;

			@Override
			public @NonNull Iterator<T> iterator() {
				if (iteratorUsed) {
					throw new RuntimeException("This iterator can only be used once");
				}
				iteratorUsed = true;
				return iterator;
			}
		};
	}

	public static <T extends @NonNull Object> Iterable<T> createPollOnceIterable(Supplier<? extends @Nullable T> supplier) {
		return wrapOnceIterator(createPollIterator(supplier));
	}

	public static <T extends @NonNull Object> Iterator<T> createPollIterator(Supplier<? extends @Nullable T> supplier) {
		return new Iterator<>() {
			boolean didFetch = false;
			@Nullable T next = null;

			@Override
			public boolean hasNext() {
				if (!didFetch) {
					next = supplier.get();
					didFetch = true;
				}
				return next != null;
			}

			@Override
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				var r = next;
				assert r != null : "Guaranteed by above hasNext() call";
				next = null;
				didFetch = false;
				return r;
			}
		};
	}

	public static <T> T single(Iterator<T> iterator) {
		var value = iterator.next();
		if (iterator.hasNext())
			throw new RuntimeException("Iterator has more than one element: " + iterator.next());
		return value;
	}

	public static <T> T single(Iterable<T> iterable) {
		return single(iterable.iterator());
	}

}
