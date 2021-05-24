package io.esaurus.kernel.snowflake;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record TimeSource(long start, long offset, Instant epoch) {
  public TimeSource(Instant epoch) {
    this(
      System.nanoTime() / 1_000_000,
      Instant.now().toEpochMilli() - epoch.toEpochMilli(),
      Objects.requireNonNull(epoch, "epoch")
    );
  }

  public static TimeSource createDefault() {
    // 2020-01-01T00:00:00Z
    return new TimeSource(Instant.ofEpochMilli(1577836800000L));
  }

  public long ticks() { return offset + elapsed(); }

  public Duration tickDuration() {
    return Duration.ofMillis(1);
  }

  private long elapsed() {
    return (System.nanoTime() / 1_000_000) - start;
  }
}
