package dev.nautchkafe.facebook.reward.monad;

public sealed interface Either<L, R> {

    record Left<L, R>(L value) implements Either<L, R> {}
    record Right<L, R>(R value) implements Either<L, R> {}
    
    static <L, R> Either<L, R> left(final L value) {
        return new Left<>(value);
    }
    
    static <L, R> Either<L, R> right(final R value) {
        return new Right<>(value);
    }
    
    default <T> T either(final Function<L, T> leftFunc, final Function<R, T> rightFunc) {
        return this instanceof Left<L, R> left 
            ? leftFunc.apply(left.value())
            : rightFunc.apply(((Right<L, R>) this).value());
    }
}

public record Tuple<A, B>(A first, B second) {

    public static <A, B> Tuple<A, B> of(final A a, final B b) {
        return new Tuple<>(a, b);
    }
}