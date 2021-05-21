package io.esaurus.service.kernel;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static io.vertx.core.buffer.Buffer.buffer;

public interface Transaction {
  static Transaction log(SqlClient client, EventBus bus, EventLog log) {
    return new Log(client, bus, log);
  }

  Future<Void> commit();

  final class Log implements Transaction {
    private static final String INSERT = """
      insert into transaction_logs(event, model, data, timepoint)
      values (#{event}, #{model}, #{data}, #{timepoint})
      """;

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
  }
}
