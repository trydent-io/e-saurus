package io.esaurus.kernel;

import io.vertx.core.Future;

public interface Intention<R extends Record> {
  Future<Void> apply(R schema);
}
