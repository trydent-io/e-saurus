package io.esaurus.service.application.change;

import io.esaurus.service.domain.Electricity;
import io.esaurus.kernel.Change;
import io.esaurus.kernel.Transactions;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

public sealed interface Drained extends Change {
  static Drained event(Electricity electricity, Instant drawnAt) {
    return new Event(electricity, drawnAt);
  }

  final class Event implements Drained {
    private static final Logger log = LoggerFactory.getLogger(Event.class);

    private final Electricity electricity;
    private final Instant drainedAt;

    private Event(final Electricity electricity, final Instant drainedAt) {
      this.electricity = electricity;
      this.drainedAt = drainedAt;
    }

    @Override
    public final Future<Void> apply(final Transactions transactions) {
      return apply(transactions, "electricity-drained", asBytes())
        .onSuccess(ignored -> log.info("Change {} committed", "drained"))
        .onFailure(cause -> log.error("Can't commit change %s".formatted("drained"), cause));
    }

    private byte[] asBytes() {
      return Json
        .encode(
          Map.of(
            "electricity", electricity.value(),
            "drainedAt", drainedAt
          )
        )
        .getBytes();
    }
  }
}
