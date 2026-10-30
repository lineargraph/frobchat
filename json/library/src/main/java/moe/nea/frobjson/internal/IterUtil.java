package moe.nea.frobjson.internal;

import java.util.Iterator;

public class IterUtil {
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
