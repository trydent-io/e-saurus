package io.esaurus.service.application.operation;

import io.esaurus.service.domain.Electricity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public interface Inject {

  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull Inject with(Electricity electricity, Instant instant) {
    return new Command(electricity, instant);
  }

  final class Command implements Inject {
    private final Electricity electricity;
    private final Instant when;

    private Command(final Electricity electricity, final Instant when) {
      this.electricity = electricity;
      this.when = when;
    }
  }
}
