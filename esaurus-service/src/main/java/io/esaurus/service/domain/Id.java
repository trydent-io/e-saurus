package io.esaurus.service.domain;

import io.esaurus.kernel.snowflake.Snowflake;

import java.util.Optional;

public final class Id {
  public final long value;

  private Id(final long value) {this.value = value;}

  public static Id next() {
    return Optional.of(Snowflake.createDefault(512))
      .map(Snowflake::next)
      .map(Id::new)
      .orElseThrow(() -> new IllegalStateException("Can't generate id"));
  }
}
