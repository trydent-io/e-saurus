package io.esaurus.kernel;

import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;

public interface Transactions {
  static Transactions journal(SqlClient client, EventBus bus) {
    return new Journal(client, bus);
  }

  Transactions aggregate(Resource model);

  Transaction append(Resource event, byte[] data);

  final class Journal implements Transactions {
    private final SqlClient client;
    private final EventBus bus;

    private Journal(final SqlClient client, EventBus bus) {
      this.client = client;
      this.bus = bus;
    }

    @Override
    public Transactions aggregate(final Resource model) {
      return null;
    }

    @Override
    public Transaction append(Resource event, byte[] data) {
      return EventLog
        .create(event, data)
        .map(log -> Transaction.append(client, bus, log))
        .orElseThrow(() -> new IllegalStateException("Can't append event %s".formatted(event)));
    }
  }
}
