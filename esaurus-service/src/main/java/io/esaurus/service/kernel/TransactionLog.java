package io.esaurus.service.kernel;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import static io.vertx.core.buffer.Buffer.buffer;
import static java.nio.charset.StandardCharsets.UTF_8;

public interface TransactionLog {
  static TransactionLog entry(SqlClient client, EventBus bus, EventLog log) {
    return new Entry(client, bus, log);
  }

  Future<Void> commit();

  interface Fact {
    default byte[] asBytes() {
      return Json.encode(this).getBytes(UTF_8);
    }

    default String $qualifiedName() {
      return this.getClass().getCanonicalName();
    }
  }

  final class Entry implements TransactionLog {
    private static final String INSERT = """
      insert into transaction_logs(event_name, event_data, model_id, model_name)
      values (`#{event_name}`, #{event_data}, #{model_id}, `#{model_name}`)
      """;

    private final SqlClient client;
    private final EventBus bus;
    private final EventLog log;

    private Entry(final SqlClient client, final EventBus bus, final EventLog log) {
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
