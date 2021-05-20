package io.esaurus.service.kernel;

import io.vertx.core.Future;

import java.util.function.Function;

public interface Operation extends Function<TransactionLogs, Future<Void>> {}
