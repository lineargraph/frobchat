package moe.nea.frobmatrix;

import java.util.function.Function;

public class Either<L, R> {
	private final L left;
	private final R right;

	private Either(L left, R right) {
		this.left = left;
		this.right = right;
		assert left == null || right == null;
		assert !(left == null && right == null);
	}

	public boolean isLeft() {
		return left != null;
	}

	public boolean isRight() {
		return right != null;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	public <T> T choose(Function<L, T> onLeft, Function<R, T> onRight) {
		if (isLeft())
			return onLeft.apply(getLeft());
		return onRight.apply(getRight());
	}

	public static <L, R> Either<L, R> ofLeft(L left) {
		return new Either<>(left, null);
	}

	public static <L, R> Either<L, R> ofRight(R right) {
		return new Either<>(null, right);
	}
}
