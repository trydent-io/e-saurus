package io.esaurus.kernel;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.templates.RowMapper;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public sealed interface Model extends Streamable<Transaction.Entry> {
  record PastEvent(String event, JsonObject data) {
    public enum Selected implements RowMapper<PastEvent> {
      Entry;

      @Override
      public PastEvent map(final Row row) {
        return new PastEvent(
          row.getString("event_name"),
          row.getBuffer("data").toJsonObject()
        );
      }
    }
  }

  static Model find(Database database, long id, String name) {
    return new Db(database, id, name);
  }

  Future<Transaction> submit(Function<? super Stream<PastEvent>, ? extends Operation> operations);

  final class Db implements Model {
    private static final String SELECT = """
      select  event_name, data
      from    transactions
      where   model_id = #{modelId}
        and   (#{modelName} is null or model_name = #{modelName})
      order by timepoint
      """;

    private final Database database;
    private final long id;
    private final String name;

    private Db(final Database database, final long id, final String name) {
      this.database = database;
      this.id = id;
      this.name = name;
    }

    @Override
    public Future<Transaction> submit(final String event, final byte[] data) {
      return database
        .select(SELECT, Map.of("modelId", id, "modelName", name), Transaction.Entry.Selected.Entry)
        .map(entries -> Transaction.submit(database, event, data));
    }
  }
}
