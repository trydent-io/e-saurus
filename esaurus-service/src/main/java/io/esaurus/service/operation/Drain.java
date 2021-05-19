package io.esaurus.service.operation;

import io.esaurus.service.domain.Electricity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public interface Drain {

  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull Drain of(Electricity electricity, Instant timepoint) {
    return new Command(electricity, timepoint);
  }

  final class Command implements Drain {
    private final Electricity electricity;
    private final Instant timepoint;

    private Command(final Electricity electricity, final Instant timepoint) {
      this.electricity = electricity;
      this.timepoint = timepoint;
    }
  }
}
