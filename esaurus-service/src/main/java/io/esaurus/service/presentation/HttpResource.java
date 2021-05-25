package io.esaurus.service.presentation;

import io.esaurus.kernel.Transaction;
import io.esaurus.kernel.Transaction.Entry;
import io.esaurus.kernel.Transactions;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Comparator;

import static java.util.Comparator.comparing;

public interface HttpResource extends Handler<RoutingContext> {

  final class Electricity implements HttpResource {
    private final Transactions transactions;

    public Electricity(final Transactions transactions) {this.transactions = transactions;}

    @Override
    public void handle(final RoutingContext resource) {
      final var id = Long.parseLong(resource.pathParam("id"));
      final var model = resource.pathParam("model");

      transactions
        .model(id, model)
        .claim(entries -> entries
          .max(comparing(Entry::eventId))
          .filter(entry -> entry.data() != null)
          .isPresent()
        )
        .submit("electricity-drained", new JsonObject().put("electricity", 12).toBuffer().getBytes())
        .commit()
        .onSuccess(ignored -> resource.response().end())
        .onFailure(resource::fail);
    }
  }
}
