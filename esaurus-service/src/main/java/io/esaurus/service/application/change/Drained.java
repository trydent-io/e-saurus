package io.esaurus.service.application.change;

import io.esaurus.service.domain.Electricity;
import io.esaurus.kernel.Change;
import io.esaurus.kernel.Resource;
import io.esaurus.kernel.Transaction;
import io.esaurus.kernel.Transactions;
import io.vertx.core.Future;
import io.vertx.core.json.Json;

import java.time.Instant;
import java.util.Map;

public sealed interface Drained extends Change {
  static Drained event(Electricity electricity, Instant drawnAt) {
    return new Event(electricity, drawnAt);
  }

  final class Event implements Drained {
    private final Electricity electricity;
    private final Instant drainedAt;

    private Event(final Electricity electricity, final Instant drainedAt) {
      this.electricity = electricity;
      this.drainedAt = drainedAt;
    }

    @Override
    public final Future<Void> apply(final Transactions transactions) {
      return apply(transactions, "electricity-drained", asBytes());
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
