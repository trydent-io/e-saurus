package io.esaurus.service.application.change;

import io.esaurus.kernel.Fact;
import io.esaurus.kernel.Outbox;
import io.esaurus.service.domain.Electricity;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

public sealed interface Drained extends Fact<Drained.Event.Schema> {
  static Drained event(Outbox outbox) {
    return new Event(outbox);
  }

  default void apply(final Electricity electricity, final Instant drainedAt) {
    apply(new Event.Schema(electricity, drainedAt));
  }

  final class Event implements Drained {
    private static final Logger log = LoggerFactory.getLogger(Event.class);

    private final Outbox outbox;

    public Event(final Outbox outbox) {
      this.outbox = outbox;
    }

    @Override
    public final void apply(final Schema schema) {
      return outbox.dispatch("electricity-drained", asBytes(schema));
    }

    private byte[] asBytes(Schema schema) {
      return Json
        .encode(
          Map.of(
            "electricity", schema.electricity.value(),
            "drainedAt", schema.drainedAt
          )
        )
        .getBytes();
    }

    private record Schema(Electricity electricity, Instant drainedAt) {}
  }
}
