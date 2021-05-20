package io.esaurus.service.operation;

import io.esaurus.service.domain.Electricity;
import io.esaurus.service.kernel.Operation;
import io.esaurus.service.kernel.Transactions;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public interface Drain extends Operation {

  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull Drain command(Electricity electricity, Instant timepoint) {
    return new Command(electricity, timepoint);
  }

  final class Command implements Drain {
    private static final Logger log = LoggerFactory.getLogger(Command.class);

    private final Electricity electricity;
    private final Instant timepoint;

    private Command(final Electricity electricity, final Instant timepoint) {
      this.electricity = electricity;
      this.timepoint = timepoint;
    }

    @Override
    public Future<Void> apply(Transactions logs, URI model) {
      return logs
        .append(
          "electricity-drawn",
          Json.encode(
            Map.of(
              "electricity", electricity.value(),
              "at", timepoint
            )
          ).getBytes(),
          model
        )
        .commit()
        .onSuccess(ignored -> log.info("Drain command submitted"))
        .onFailure(cause -> log.error("Can't submit drain command", cause));
    }
  }
}
