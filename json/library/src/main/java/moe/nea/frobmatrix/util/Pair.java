package moe.nea.frobmatrix.util;

import java.io.Serializable;

public record Pair<L, R>(
	L left,
	R right
) implements Serializable {
	public static <L, R> Pair<L, R> of(L left, R right) {
		return new Pair<>(left, right);
	}
}
