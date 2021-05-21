package io.esaurus.kernel;


import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;

public interface Migration {

  static Migration create(SqlClient client) {
    return new Db(client);
  }

  Future<Transactions> transactionLogs(EventBus eventBus);

  final class Db implements Migration {
    private static final String CREATE = """
      create table if not exists transaction_logs(
        id identity primary key,
        event varchar(255) not null,
        fact  blob not null,
        model varchar(255),
        timepoint timestamp not null
      )
      """;

    private final SqlClient client;

    private Db(SqlClient client) {
      this.client = client;
    }

    @Override
    public final Future<Transactions> transactionLogs(final EventBus eventBus) {
      return SqlTemplate
        .forUpdate(client, CREATE)
        .execute(Map.of())
        .map(Transactions.journal(client, eventBus));
    }
  }
}
