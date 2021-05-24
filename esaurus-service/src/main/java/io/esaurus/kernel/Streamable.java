package io.esaurus.kernel;

import io.vertx.core.Future;

import java.util.stream.Stream;

public interface Streamable<R extends Record> {
  Future<Stream<R>> stream();
}
