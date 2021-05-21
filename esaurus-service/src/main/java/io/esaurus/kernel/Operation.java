package io.esaurus.kernel;

import io.vertx.core.Future;

public interface Operation {
  Future<Void> apply(Transactions logs);
}
