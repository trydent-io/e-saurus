package io.esaurus.kernel;

import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.templates.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.vertx.core.Future.*;
import static java.time.ZoneOffset.UTC;

public interface Transaction {
  static Transaction create(Database database, String eventName, byte[] data) {
    return new Submitted(database, eventName, data);
  }

  static Transaction create(Database database, String eventName, byte[] data, long modelId, String modelName) {
    return new Submitted(database, eventName, data, modelId, modelName);
  }

  Future<Void> commit();

  record Entry(long eventId, String eventName, byte[] data, long modelId, String modelName, Instant persistedAt) {
    public enum RowAs implements RowMapper<Row> {
      Is;

      @Override
      public Row map(final Row row) {
        return row;
      }
    }

    public enum EntryFrom implements RowMapper<Entry> {
      Row;

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

  final class Submitted implements Transaction {
    private static final Logger log = LoggerFactory.getLogger(Submitted.class);

    private static final String INSERT = """
      insert into transactions(event_id, event_name, data, model_id, model_name)
      values (#{eventId}, #{eventName}, #{data}, #{modelId}, #{modelName})
      """;
    private static final String SELECT = """
      select    event_id, event_name, model_id, model_name, data, timepoint
      from      transactions
      where     model_id = #{modelId} and (#{modelName} is null or model_name = #{modelName})
      """;

    private final Database database;
    private final String eventName;
    private final byte[] data;
    private final long modelId;
    private final String modelName;

    private Submitted(final Database database, final String eventName, final byte[] data) {
      this(database, eventName, data, -1, null);
    }

    private Submitted(final Database database, final String eventName, final byte[] data, final long modelId, final String modelName) {
      this.database = database;
      this.eventName = eventName;
      this.data = data;
      this.modelId = modelId;
      this.modelName = modelName;
    }

    @Override
    public Future<Void> commit() {
      return database
        .insert(
          INSERT,
          Map.of(
            "eventId", UUID.randomUUID(),
            "eventName", eventName,
            "data", data,
            "modelId", modelId,
            "modelName", modelName
          )
        )
        .map(outbox -> outbox.dispatch(eventName, data))
        .onSuccess(ignored -> log.info("{} dispatched", eventName))
        .onFailure(cause -> log.error("Can't dispatch event %s".formatted(eventName), cause))
        .mapEmpty();
    }
  }

  final class Asserted implements Transaction {
    private final Model model;
    private final Transaction transaction;
    private final Predicate<Stream<Transaction.Entry>> assertion;

    public Asserted(final Model model, final Transaction transaction, final Predicate<Stream<Entry>> assertion) {
      this.model = model;
      this.transaction = transaction;
      this.assertion = assertion;
    }

    @Override
    public Future<Void> commit() {
      return model.stream()
        .flatMap(entries -> assertion.test(entries)
          ? transaction.commit()
          : failedFuture("Can't commit for unmet claim")
        );
    }
  }
}
