package io.esaurus.kernel;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Iterator;
import java.util.UUID;

import static io.esaurus.kernel.Transaction.Entry;
import static io.vertx.core.buffer.Buffer.buffer;

public interface Transaction extends Iterable<Entry> {
  static Transaction append(SqlClient client, EventBus bus, EventLog log) {
    return new Log(client, bus, log);
  }

  Future<Void> commit();

  final class Log implements Transaction {
    private static final String INSERT = "insert into transactions(event, model, data) values (#{event}, #{model}, #{data})";

    private final SqlClient client;
    private final EventBus bus;
    private final EventLog log;

    private Log(final SqlClient client, final EventBus bus, final EventLog log) {
      this.client = client;
      this.bus = bus;
      this.log = log;
    }

    @Override
    public Future<Void> commit() {
      return SqlTemplate.forUpdate(client, INSERT)
        .execute(log.asMap())
        .<Void>mapEmpty()
        .onSuccess(ignored -> bus.publish(log.event().getAuthority(), buffer(log.data())))
        .onFailure(cause -> bus.publish(log.event().getAuthority() + "-failed", buffer(log.data())));
    }

    @NotNull
    @Override
    public Iterator<Entry> iterator() {
      return null;
    }
  }

  record Entry(String eventName, byte[] data, UUID modelId, String modelName, Instant persistedAt) {}
}
