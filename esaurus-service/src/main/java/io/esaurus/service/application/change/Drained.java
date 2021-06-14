package io.esaurus.service.application.change;

import io.esaurus.kernel.Fact;
import io.esaurus.kernel.Outbox;
import io.esaurus.service.domain.Electricity;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static io.vertx.core.Future.succeededFuture;

public sealed interface Drained extends Fact<Drained.Event.Schema> {
  static Drained event(Outbox outbox) {
    return new Event(outbox);
  }

  default Future<Void> apply(final Electricity electricity, final Instant drainedAt) {
    return apply(new Event.Schema(electricity, drainedAt));
  }

  final class Event implements Drained {
    private static final Logger log = LoggerFactory.getLogger(Event.class);

    private final Outbox outbox;

    public Event(final Outbox outbox) {
      this.outbox = outbox;
    }

    @Override
    public final Future<Void> apply(final Schema schema) {
      return succeededFuture(outbox.dispatch("electricity-drained", schema))
        .<Void>mapEmpty()
        .onSuccess(ignored -> log.info("Electricity drained dispatched"))
        .onFailure(cause -> log.error("Can't dispatch electricity drained", cause));
    }

    private record Schema(Electricity electricity, Instant drainedAt) {}
  }
}
