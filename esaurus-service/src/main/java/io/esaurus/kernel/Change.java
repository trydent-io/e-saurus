package io.esaurus.kernel;


import io.vertx.core.Future;

public interface Change {
  Future<Void> apply(Transactions transactions);

  default Future<Void> apply(Transactions transactions, String name, byte[] data) {
    return Unity
      .event(name)
      .map(resource -> transactions.submit(resource, data))
      .map(Transaction::commit)
      .orElseThrow(() -> new IllegalStateException("Can't commit change %s".formatted(name)));
  }
}
