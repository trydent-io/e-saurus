package io.esaurus.service.presentation;

import io.esaurus.kernel.Unity;
import io.esaurus.service.application.operation.Drain;
import io.esaurus.kernel.Transactions;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Function;

public interface Resource {

  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull Resource electricity(Transactions transactions, Drain drain) {
    return new Electricity(transactions, drain);
  }

  Future<Void> resolve(UUID id, String unity, final Function<Unity, Future<Void>> );

  final class Electricity implements Resource {
    private static final Logger log = LoggerFactory.getLogger(Electricity.class);

    private static final String MODEL_NAME = "electricity";

    private final Transactions transactions;
    private final Drain drain;

    @Contract(pure = true)
    private Electricity(final Transactions transactions, final Drain drain) {
      this.transactions = transactions;
      this.drain = drain;
    }

    @Override
    public final Future<Void> resolve(UUID id) {
      return Unity
        .model(id, MODEL_NAME)
        .map(transactions::aggregate)
        .map(drain::apply)
        .orElseThrow()
        .onSuccess(ignored -> log.info("Send drain-command on model {}", MODEL_NAME))
        .onFailure(cause -> log.error("Can't send drain-command on model %s".formatted(MODEL_NAME), cause));
    }

    @Override
    public void handle(final RoutingContext electricity) {
      electricity
    }
  }
}
