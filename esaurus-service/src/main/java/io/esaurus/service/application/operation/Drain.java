package io.esaurus.service.application.operation;

import io.esaurus.service.application.change.Drained;
import io.esaurus.service.domain.Electricity;
import io.esaurus.kernel.Operation;
import io.esaurus.kernel.Resource;
import io.esaurus.kernel.Transactions;
import io.vertx.core.Future;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public sealed interface Drain extends Operation {

  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull Drain command(Electricity electricity, Instant timepoint) {
    return new Command(electricity, timepoint);
  }

  final class Command implements Drain {
    private static final Logger log = LoggerFactory.getLogger(Drain.class);

    private final Electricity electricity;
    private final Instant timepoint;

    private Command(final Electricity electricity, final Instant timepoint) {
      this.electricity = electricity;
      this.timepoint = timepoint;
    }

    @Override
    public Future<Void> apply(Transactions transactions) {
      return Drained
        .event(electricity, timepoint)
        .apply(transactions)
        .onSuccess(ignored -> log.info("Drain command submitted"))
        .onFailure(cause -> log.error("Can't submit drain command", cause));
    }
  }
}
