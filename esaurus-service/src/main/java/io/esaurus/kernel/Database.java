package io.esaurus.kernel;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;

public sealed interface Database {
  static Database sql(SqlClient client, EventBus bus) {
    return new Sql(client, bus);
  }

  Future<Void> update(String query, )

  final class Sql implements Database {
    private final SqlClient client;
    private final EventBus bus;

    private Sql(final SqlClient client, final EventBus bus) {
      this.client = client;
      this.bus = bus;
    }


  }
}
