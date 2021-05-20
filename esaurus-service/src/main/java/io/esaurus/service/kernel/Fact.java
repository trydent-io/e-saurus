package io.esaurus.service.kernel;

import io.vertx.core.json.Json;

import static java.nio.charset.StandardCharsets.UTF_8;

public interface Fact {
  default byte[] asBytes() {
    return Json.encode(this).getBytes(UTF_8);
  }

  default String $qualifiedName() {
    return this.getClass().getCanonicalName();
  }
}
