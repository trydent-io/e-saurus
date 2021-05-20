package io.esaurus.service.kernel;

import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;

import java.util.Optional;

public interface TransactionLogs {
  static TransactionLogs journal(SqlClient client, EventBus bus) {
    return new Journal(client, bus);
  }

  default TransactionLog append(String eventName, byte[] data) {
    return append(eventName, data, Long.MIN_VALUE, null);
  }

  TransactionLog append(String event, byte[] data, long modelId, String modelName);

  final class Journal implements TransactionLogs {
    private final SqlClient client;
    private final EventBus bus;

    private Journal(final SqlClient client, EventBus bus) {
      this.client = client;
      this.bus = bus;
    }

    @Override
    public TransactionLog append(final String event, final byte[] data, final long modelId, final String modelName) {
      return EventLog
        .create(event, data, modelId, modelName)
        .map(log -> TransactionLog.entry(client, bus, log))
        .orElseThrow(() -> new IllegalStateException("Can't append event %s".formatted(event)));
    }
  }
}
