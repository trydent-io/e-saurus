package io.esaurus.kernel;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.RowMapper;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Stream;

import static io.vertx.core.buffer.Buffer.buffer;
import static java.util.stream.StreamSupport.stream;

public sealed interface Database {
  static Database client(SqlClient client, EventBus bus) {
    return new Client(client, Outbox.event(bus));
  }

  Future<Outbox> insert(String insert, Map<String, Object> params);

  Future<Outbox> update(String update, Map<String, Object> params);

  <R extends Record> Future<Stream<R>> select(String select, Map<String, Object> params, RowMapper<R> result);

  final class Client implements Database {
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    private final SqlClient client;
    private final Outbox outbox;

    public Client(final SqlClient client, final Outbox outbox) {
      this.client = client;
      this.outbox = outbox;
    }

    private Future<Outbox> modify(String modify, Map<String, Object> params) {
      return SqlTemplate.forUpdate(client, modify)
        .execute(params)
        .map(outbox);
    }

    @Override
    public Future<Outbox> insert(String insert, Map<String, Object> params) {
      return modify(insert, params)
        .onSuccess(ignored -> log.info("Insert {} executed", insert))
        .onFailure(cause -> log.error("Can't execute insert %s".formatted(insert), cause));
    }

    @Override
    public Future<Outbox> update(String update, Map<String, Object> params) {
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
