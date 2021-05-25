package io.esaurus.kernel;

import io.vertx.core.Future;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.esaurus.kernel.Transaction.Entry.EntryFrom;

public sealed interface Model extends Streamable<Transaction.Entry> {

  static Model create(Database database, long id, String name) {
    return new Db(database, id, name);
  }

  Model claim(Predicate<Stream<Transaction.Entry>> assertion);
  Transaction submit(final String event, final byte[] data);

  final class Db implements Model {
    private static final String SELECT = """
      select  event_id, event_name, data, model_id, model_name
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
    public Model claim(final Predicate<Stream<Transaction.Entry>> assertion) {
      return null;
    }

    @Override
    public Transaction submit(final String event, final byte[] data) {
      return Transaction.create(database, event, data, id, name);
    }

    @Override
    public Future<Stream<Transaction.Entry>> stream() {
      return database.select(SELECT, Map.of("modelId", id, "modelName", name), EntryFrom.Row);
    }
  }

  final class Claimed implements Model {
    private final Model model;
    private final Predicate<Stream<Transaction.Entry>> assertion;

    public Claimed(final Model model, final Predicate<Stream<Transaction.Entry>> assertion) {
      this.model = model;
      this.assertion = assertion;
    }

    @Override
    public Model claim(final Predicate<Stream<Transaction.Entry>> assertion) {
      return new Claimed(this, assertion);
    }

    @Override
    public Transaction submit(final String event, final byte[] data) {
      return new Transaction.Asserted(model, model.submit(event, data), assertion);
    }

    @Override
    public Future<Stream<Transaction.Entry>> stream() {
      return model.stream();
    }
  }
}
