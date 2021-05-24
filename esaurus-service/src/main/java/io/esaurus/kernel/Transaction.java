package io.esaurus.kernel;

import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.templates.RowMapper;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

import static io.esaurus.kernel.Transaction.Entry;
import static io.vertx.core.buffer.Buffer.buffer;
import static java.time.ZoneOffset.UTC;

public interface Transaction extends Iterable<Entry> {
  record Entry(long eventId, String eventName, byte[] data, long modelId, String modelName, Instant persistedAt) {
    public enum Selected implements RowMapper<Entry> {
      Entry;

      @Override
      public Entry map(final Row row) {
        return new Entry(
          row.getLong("eventId"),
          row.getString("eventName"),
          row.getBuffer("data").getBytes(),
          row.getLong("modelId"),
          row.getString("modelName"),
          row.getLocalDateTime("persistedAt").toInstant(UTC)
        );
      }
    }
  }
  static Transaction submit(Database database, String eventName, byte[] data) {
    return new Submitted(database, eventName, data);
  }

  Future<Void> commit();

  final class Submitted implements Transaction {
    private static final String INSERT = """
      insert into transactions(eventId, eventName, data)
      values (#{eventId}, #{eventName}, #{data})
      """;

    private final Database database;
    private final String eventName;
    private final byte[] data;

    private Submitted(final Database database, final String eventName, final byte[] data) {
      this.database = database;
      this.eventName = eventName;
      this.data = data;
    }

    @Override
    public Future<Void> commit() {
      return database.insert(INSERT, Map.of())
        .compose(ignored -> database.select(SELECT, ))
        .onSuccess(ignored -> bus.publish(log.event().getAuthority(), buffer(log.data())))
        .onFailure(cause -> bus.publish(log.event().getAuthority() + "-failed", buffer(log.data())));
    }

    @NotNull
    @Override
    public Iterator<Entry> iterator() {
      return null;
    }
  }
}
