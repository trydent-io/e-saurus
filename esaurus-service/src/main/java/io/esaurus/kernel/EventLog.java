package io.esaurus.kernel;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static io.esaurus.kernel.Params.having;
import static io.esaurus.kernel.Params.maybe;

record EventLog(UUID id, URI event, URI model, byte[] data, Instant timepoint) {
  public EventLog {
    assert id != null && event != null && data != null && data.length > 0 && timepoint != null;
  }

  public EventLog(URI event, URI model, byte[] data) {
    this(UUID.randomUUID(), event, model, data, Instant.now());
  }

  public EventLog(URI event, byte[] data) {
    this(event, null, data);
  }

  static Optional<EventLog> create(URI event, URI model, byte[] datum) {
    return having(
      maybe(event),
      maybe(model).or(() -> Resource.empty().map(Resource::value)),
      maybe(datum).filter(bytes -> bytes.length > 0),
      it -> Resource.none().is(it.second())
        ? new EventLog(it.first(), it.third())
        : new EventLog(it.first(), it.second(), it.third())
    );
  }

  public final Map<String, Object> asMap() {
    return Map.ofEntries(
      Map.entry("id", id),
      Map.entry("event", event.toString()),
      Map.entry("model", model.toString()),
      Map.entry("data", data),
      Map.entry("timepoint", timepoint)
    );
  }
}
