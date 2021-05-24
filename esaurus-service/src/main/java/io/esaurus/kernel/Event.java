package io.esaurus.kernel;

import io.esaurus.service.domain.Id;

import java.net.URI;
import java.util.Optional;

public record Event(URI value) {
  public static final String EVENT = "event://%s#%d";

  public Event { assert value != null; }

  public static Optional<Event> of(String name) {
    return Optional.ofNullable(name)
      .map(String::toLowerCase)
      .map(it -> URI.create(EVENT.formatted(it, Id.next().value)))
      .map(Event::new);
  }

  public String name() { return value.getAuthority(); }
  public long id() { return Long.getLong(value.getFragment()); }
}
