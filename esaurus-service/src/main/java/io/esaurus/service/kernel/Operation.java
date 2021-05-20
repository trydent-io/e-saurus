package io.esaurus.service.kernel;

import io.vertx.core.Future;

import java.net.URI;

public interface Operation {
  Future<Void> apply(Transactions logs, URI model);
  default Future<Void> apply(Transactions logs) {
    return apply(logs, null);
  }
}
