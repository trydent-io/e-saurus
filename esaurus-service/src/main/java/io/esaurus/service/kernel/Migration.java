package io.esaurus.service.kernel;


import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;

public interface Migration {

  static Migration with(SqlClient client) {
    return new Sql(client);
  }

  Future<Journal> journal();

  final class Sql implements Migration {
    private static final String CREATE_LOGS_TABLE = """
      create table if not exists
      """;

    private final SqlClient client;

    private Sql(SqlClient client) {
      this.client = client;
    }

    @Override
    public final Future<Journal> journal() {
      return SqlTemplate
        .forUpdate(client, CREATE_LOGS_TABLE)
        .execute(Map.of())
        .map(Journal.from(client));
    }
  }
}
