package io.esaurus.kernel;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;

public record Unity(URI value) {
  public Unity { assert value != null; }

  public boolean is(URI value) {
    return this.value.equals(value);
  }

  public static Optional<Unity> model(final UUID id, final String name) {
    return resource("model", id, name);
  }

  public static Optional<Unity> event(final String name) {
    return resource("event", UUID.randomUUID(), name);
  }

  public static Unity none() {
    return new Unity(URI.create("empty:none"));
  }

  public static Optional<Unity> empty() {
    return Optional.of(none());
  }

  private static Optional<Unity> resource(final String resource, final UUID id, final String name) {
    return Optional.ofNullable(name)
      .map(it -> {
        try {
          return new URI(resource, it, UUID.randomUUID().toString());
        } catch (URISyntaxException e) {
          return null;
        }
      })
      .map(Unity::new);
  }
}
