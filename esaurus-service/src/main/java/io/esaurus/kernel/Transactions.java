package io.esaurus.kernel;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.StreamSupport.stream;

public interface Transactions {

  static Transactions source(Database database) {
    return new Source(database);
  }

  Transactions aggregate(Resource model);

  Transaction append(Resource event, byte[] data);

  Future<Iterable<Transaction.Entry>> iterate();

  final class Source implements Transactions {
    private final Database database;

    private Source(final Database database) {
      this.database = database;
    }

    @Override
    public Transactions aggregate(final Resource model) {
      return null;
    }

    @Override
    public Transaction append(Resource event, byte[] data) {
      return Transaction.append(database, event, data))
        .orElseThrow(() -> new IllegalStateException("Can't append event %s".formatted(event)));
    }

    @Override
    public Future<Iterable<Transaction.Entry>> iterate() {
      record Result(URI event, URI model, byte[] data, Instant timepoint) {
        Result(String event, String model, byte[] data, Instant timepoint) {
          this(URI.create(event), URI.create(model), data, timepoint);
        }
      }
      return database
        .select("select event, model, data, timepoint from transactions", Map.of(), Result.class)
        .map(results ->
          stream(results.spliterator(), false)
            .map(result ->
              new Transaction.Entry(
                result.event.getAuthority(),
                result.data,
                UUID.fromString(result.model.getFragment()),
                result.model.getAuthority(),
                result.timepoint
              )
            )
            .toList()
        );
    }

    @NotNull
    @Override
    public Iterator<Transaction.Entry> iterator() {
      return null;
    }
  }
}
