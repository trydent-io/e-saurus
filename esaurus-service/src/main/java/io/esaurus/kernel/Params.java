package io.esaurus.kernel;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public sealed interface Params {
  enum Comprehension implements Params {}

  record AB<A, B>(A first, B second) {}
  record ABC<A, B, C>(A first, B second, C third) {}
  record ABCD<A, B, C, D>(A first, B second, C third, D forth) {}
  record ABCDE<A, B, C, D, E>(A first, B second, C third, D forth, E fifth) {}

  static <T> Optional<T> maybe(T value) { return Optional.ofNullable(value); }

  static <A, B, R> Optional<R> having(Optional<A> maybeA, Optional<B> maybeB, Function<? super AB<A, B>, ? extends R> func) {
    return maybeA.flatMap(a -> maybeB.map(b -> func.apply(new AB<>(a, b))));
  }

  static <A, B, C, R> Optional<R> having(Optional<A> maybeA, Optional<B> maybeB, Optional<C> maybeC, Function<? super ABC<A, B, C>, ? extends R> func) {
    return maybeA.flatMap(a -> maybeB.flatMap(b -> maybeC.map(c -> func.apply(new ABC<>(a, b, c)))));
  }

  static <A, B, C, D, R> Optional<R> having(Optional<A> maybeA, Optional<B> maybeB, Optional<C> maybeC, Optional<D> maybeD, Function<? super ABCD<A, B, C, D>, ? extends R> func) {
    return maybeA.flatMap(a -> maybeB.flatMap(b -> maybeC.flatMap(c -> maybeD.map(d -> func.apply(new ABCD<>(a, b, c, d))))));
  }

  static <A, B, C, D, E, R> Optional<R> having(Optional<A> maybeA, Optional<B> maybeB, Optional<C> maybeC, Optional<D> maybeD, Optional<E> maybeE, Function<? super ABCDE<A, B, C, D, E>, ? extends R> func) {
    return maybeA.flatMap(a -> maybeB.flatMap(b -> maybeC.flatMap(c -> maybeD.flatMap(d -> maybeE.map(e -> func.apply(new ABCDE<>(a, b, c, d, e)))))));
  }
}
