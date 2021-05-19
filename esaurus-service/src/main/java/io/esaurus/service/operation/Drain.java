package io.esaurus.service.operation;

import io.esaurus.service.kernel.Electricity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Drain {

  @Contract(value = "_ -> new", pure = true)
  static @NotNull Drain of(Electricity electricity) {
    return new Command(electricity);
  }

  final class Command implements Drain {
    private final Electricity electricity;

    private Command(final Electricity electricity) {this.electricity = electricity;}
  }
}
