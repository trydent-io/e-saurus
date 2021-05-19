package io.esaurus.service.kernel;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.templates.RowMapper;

import java.time.Instant;

public interface Log {

  static Log from(Row row) {
    return
  }

  final class Result implements Log, RowMapper<Transaction> {
    private final Row row;

    private Result(Row row) {
      this.row = row;
    }

    @Override
    public final Transaction map(Row row) {
      return new Transaction(
        row.getLong("id"),
        row.getLong("eventId"),
        row.getString(""),
        row.getString()
      );
    }
  }

  record Transaction(
    long id,
    long eventId,
    String eventName,
    String eventData,
    long modelId,
    String modelName,
    Instant timepoint
  ) {
  }

}