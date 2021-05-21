package io.esaurus.kernel;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;

public record Resource(URI value) {
  public Resource { assert value != null; }

  public boolean is(URI value) {
    return this.value.equals(value);
  }

  public static Optional<Resource> model(final UUID id, final String name) {
    return resource("model", id, name);
  }

  public static Optional<Resource> event(final String name) {
    return resource("event", UUID.randomUUID(), name);
  }

  public static Resource none() {
    return new Resource(URI.create("empty:none"));
  }

  public static Optional<Resource> empty() {
    return Optional.of(none());
  }

  private static Optional<Resource> resource(final String resource, final UUID id, final String name) {
    return Optional.ofNullable(name)
      .map(it -> {
        try {
          return new URI(resource, it, UUID.randomUUID().toString());
        } catch (URISyntaxException e) {
          return null;
        }
      })
      .map(Resource::new);
  }
}
