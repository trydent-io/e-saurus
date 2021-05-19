package io.esaurus.service.operation;

import io.esaurus.service.kernel.Electricity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public interface Supply {

  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull Supply of(Electricity electricity, Instant instant) {
    return new Command(electricity, instant);
  }

  final class Command implements Supply {
    private final Electricity electricity;
    private final Instant when;

    private Command(final Electricity electricity, final Instant when) {
      this.electricity = electricity;
      this.when = when;
    }
  }
}
