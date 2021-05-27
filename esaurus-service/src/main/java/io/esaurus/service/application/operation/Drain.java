package io.esaurus.service.application.operation;

import io.esaurus.kernel.Intention;
import io.esaurus.kernel.Transactions;
import io.esaurus.service.domain.Electricity;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public sealed interface Drain extends Intention<Drain.Command.Schema> {

  @Contract(value = "_ -> new", pure = true)
  static @NotNull Drain command(Transactions transactions) {
    return new Command(transactions);
  }

  default Future<Void> apply(final int electricity, final long timepoint) {
    return Electricity.of(electricity)
      .map(it -> apply(new Command.Schema(it, Instant.ofEpochMilli(timepoint))))
      .orElseThrow();
  }

  final class Command implements Drain {
    private static final Logger log = LoggerFactory.getLogger(Drain.class);

    private final Transactions transactions;

    private Command(final Transactions transactions) {this.transactions = transactions;}

    @Override
    public Future<Void> apply(final Schema schema) {
      return transactions
        .model(123L, "electricity")
        .submit(
          "electricity-drained",
          new JsonObject()
            .put("electricity", schema.electricity.value())
            .put("drainedAt", schema.timepoint)
            .toBuffer()
            .getBytes()
        )
        .commit();
    }

    private record Schema(Electricity electricity, Instant timepoint) {}
  }
}
