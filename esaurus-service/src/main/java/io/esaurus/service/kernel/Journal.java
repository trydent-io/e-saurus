package io.esaurus.service.kernel;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;

// logs
public interface Journal {

  static Journal from(SqlClient client, EventBus bus) {
    return new Sql(client, bus);
  }

  final class Sql implements Journal {
    private static final String INSERT = """
      insert into logs(event_name, event_data, model_id, model_name)
      values (`#{event_name}`, #{event_data}, #{model_id}, `#{model_name}`)
      """;

    private final SqlClient client;
    private final EventBus bus;

    private Sql(final SqlClient client, EventBus bus) {
      this.client = client;
      this.bus = bus;
    }

    public final Future<Void> store(final String eventName, final byte[] eventData, final long modelId, final String modelName) {
      final var template = SqlTemplate.forQuery(client, INSERT);
      return template
        .execute(
          Map.ofEntries(
            Map.entry("event_name", eventName),
            Map.entry("event_data", eventData),
            Map.entry("model_id", modelId),
            Map.entry("model_name", modelName)
          )
        );
    }
  }
}
