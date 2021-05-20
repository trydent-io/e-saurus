package io.esaurus.service.kernel;

import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;

import java.net.URI;

public interface Transactions {
  static Transactions journal(SqlClient client, EventBus bus) {
    return new Journal(client, bus);
  }

  Transaction append(URI event, URI model, byte[] data);
  default Transaction append(URI event, byte[] data) {
    return append(event, null, data);
  }

  final class Journal implements Transactions {
    private final SqlClient client;
    private final EventBus bus;

    private Journal(final SqlClient client, EventBus bus) {
      this.client = client;
      this.bus = bus;
    }

    @Override
    public Transaction append(URI event, URI model, byte[] data) {
      return EventLog
        .create(event, model, data)
        .map(log -> Transaction.log(client, bus, log))
        .orElseThrow(() -> new IllegalStateException("Can't append event %s".formatted(event)));
    }
  }
}
