package io.esaurus.service.application.operation;

import io.esaurus.kernel.Model;
import io.esaurus.kernel.Operation;
import io.esaurus.service.application.change.Drained;
import io.esaurus.service.domain.Electricity;
import io.vertx.core.Future;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.stream.Stream;

public sealed interface Drain extends Operation<Drain.Command.Schema> {

  @Contract(value = "_ -> new", pure = true)
  static @NotNull Drain command(Stream<Model.PastEvent> history) {
    return new Command(history);
  }

  final class Command implements Drain {
    private static final Logger log = LoggerFactory.getLogger(Drain.class);

    private final Stream<Model.PastEvent> events;

    public Command(final Stream<Model.PastEvent> events) {this.events = events;}

    @Override
    public Future<Void> apply(final Schema schema) {
      return events.count() > 0 ? ;
    }

    private record Schema(Electricity electricity, Instant timepoint) {}

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
