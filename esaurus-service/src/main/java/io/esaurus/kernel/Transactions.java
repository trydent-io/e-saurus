package io.esaurus.kernel;

import io.vertx.core.Future;

import java.util.Map;
import java.util.stream.Stream;

import static io.esaurus.kernel.Transaction.Entry.EntryFrom;

public interface Transactions extends Streamable<Transaction.Entry> {

  static Transactions from(Database database) {
    return new Db(database);
  }

  Model model(long id, String name);

  default Model model(long id) {
    return model(id, null);
  }

  Transaction submit(String event, byte[] data);

  final class Db implements Transactions {
    private static final String SELECT = """
      select    eventId, eventName, modelId, modelName, data, timepoint
      from      transactions
      """;

    private final Database database;

    private Db(final Database database) {
      this.database = database;
    }

    @Override
    public Model model(final long id, final String name) {
      return Model.create(database, id, name);
    }

    @Override
    public Transaction submit(String event, byte[] data) {
      return Transaction.create(database, event, data);
    }

    @Override
    public Future<Stream<Transaction.Entry>> stream() {
      return database.select(SELECT, Map.of(), EntryFrom.Row);
    }
  }
}
