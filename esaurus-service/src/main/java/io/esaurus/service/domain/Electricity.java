package io.esaurus.service.domain;

import java.util.Optional;

public record Electricity(int value) {
  public Electricity { assert value >= 0; }

  public static Optional<Electricity> of(int value) {
    return Optional.of(value).filter(it -> it >= 0).map(Electricity::new);
  }
}
