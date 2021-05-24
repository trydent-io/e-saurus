package io.esaurus.kernel;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.RowMapper;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

public sealed interface Database {
  static Database client(SqlClient client, EventBus bus) {
    return new Client(client, bus);
  }

  default Future<Void> insert(String insert) { return insert(insert, Map.of()); }
  default Future<Void> update(String update) { return update(update, Map.of()); }

  Future<Void> insert(String insert, Map<String, Object> params);
  Future<Void> update(String update, Map<String, Object> params);
  <R extends Record> Future<Stream<R>> select(String select, Map<String, Object> params, RowMapper<R> result);

  final class Client implements Database {
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    private final SqlClient client;
    private final EventBus bus;

    private Client(final SqlClient client, final EventBus bus) {
      this.client = client;
      this.bus = bus;
    }

    private Future<Void> modify(String modify, Map<String, Object> params) {
      return SqlTemplate.forUpdate(client, modify)
        .execute(params)
        .<Void>mapEmpty();
    }

    @Override
    public Future<Void> insert(String insert, Map<String, Object> params) {
      return modify(insert, params)
        .onSuccess(ignored -> log.info("Insert {} executed", insert))
        .onFailure(cause -> log.error("Can't execute insert %s".formatted(insert), cause));
    }

    @Override
    public Future<Void> update(String update, Map<String, Object> params) {
      return modify(update, params)
        .onSuccess(ignored -> log.info("Update {} executed", update))
        .onFailure(cause -> log.error("Can't execute update %s".formatted(update), cause));
    }

    @Override
    public <R extends Record> Future<Stream<R>> select(String select, Map<String, Object> params, RowMapper<R> result) {
      return SqlTemplate.forQuery(client, select)
        .mapTo(result)
        .execute(params)
        .map(rows -> stream(rows.spliterator(), false));
    }
  }
}
