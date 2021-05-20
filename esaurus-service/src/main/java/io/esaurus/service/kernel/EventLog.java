package io.esaurus.service.kernel;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static io.esaurus.service.kernel.Params.having;
import static io.esaurus.service.kernel.Params.maybe;

public record EventLog(UUID id, URI event, byte[] data, URI model, Instant timepoint) {
  private static final String EMPTY = "";

  private static final String EVENT_URI = "event:%s:%s";
  private static final String MODEL_URI = "model:%s:%d";

  public EventLog { assert id != null && event != null && data != null && data.length > 0 && timepoint != null; }

  public EventLog(URI event, byte[] data, URI model) { this(UUID.randomUUID(), event, data, model, Instant.now()); }

  public EventLog(URI event, byte[] data) { this(UUID.randomUUID(), event, data, null, Instant.now()); }

  static Optional<EventLog> create(String eventName, byte[] data, long modelId, String modelName) {
    return having(
      maybe(eventName).map(it -> EVENT_URI.formatted(it, UUID.randomUUID().toString())),
      maybe(data).filter(bytes -> bytes.length > 0),
      maybe(modelName).map(it -> MODEL_URI.formatted(modelName, modelId)).or(() -> Optional.of(EMPTY)),
      it -> it.third().equals(EMPTY)
        ? new EventLog(URI.create(it.first()), it.second())
        : new EventLog(URI.create(it.first()), it.second(), URI.create(it.third()))
    );
  }

  public final Map<String, Object> asMap() {
    return
      Map.ofEntries(
        Map.entry("id", id),
        Map.entry("eventName", event.getAuthority()),
        Map.entry("eventId", event.getPath()),
        Map.entry("data", data),
        Map.entry("modelName", model.getAuthority()),
        Map.entry("modelId", model.getPort())
      );
  }
}
