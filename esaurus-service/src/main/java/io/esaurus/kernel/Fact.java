package io.esaurus.kernel;

import io.vertx.core.Future;
import io.vertx.core.json.Json;

import static java.nio.charset.StandardCharsets.UTF_8;

public interface Fact<R extends Record> {
  Future<Void> apply(R schema);
}
