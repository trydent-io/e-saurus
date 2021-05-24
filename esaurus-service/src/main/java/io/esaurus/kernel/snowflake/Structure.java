package io.esaurus.kernel.snowflake;

import java.time.Duration;
import java.time.Instant;

public record Structure(int timestampBits, int generatorBits, int sequenceBits) {
  public Structure {
    if (timestampBits < 1) {
      throw new IllegalArgumentException("timestampBits must no be <= 0, but was " + timestampBits);
    }
    if (generatorBits < 1 || generatorBits > 31) {
      throw new IllegalArgumentException("generatorBits must be between 1 (inclusive) and 31 (inclusive), but was " + generatorBits);
    }
    if (sequenceBits < 1 || sequenceBits > 31) {
      throw new IllegalArgumentException("sequenceBits must be between 1 (inclusive) and 31 (inclusive), but was " + sequenceBits);
    }

    var sum = timestampBits + generatorBits + sequenceBits;
    if (sum != 63) {
      throw new IllegalArgumentException("timestampBits + generatorBits + sequenceBits must be 63, but was " + sum);
    }
  }

  /**
   * Creates a structure with default settings.
   * <p>
   * Uses 41 bits for the timestamp, 10 for the generator id and 12 for the sequence.
   *
   * @return structure
   */
  public static Structure createDefault() {
    return new Structure(41, 10, 12);
  }

  public long maxGenerators() {
    return 1L << generatorBits;
  }

  public long maxSequenceIds() {
    return 1L << sequenceBits;
  }

  public long maxTimestamps() {
    return 1L << timestampBits;
  }

  public Duration calculateWraparoundDuration(TimeSource timeSource) {
    return timeSource.tickDuration().multipliedBy(maxTimestamps());
  }

  public Instant calculateWraparoundDate(TimeSource timeSource) {
    return timeSource.epoch().plus(calculateWraparoundDuration(timeSource));
  }
}
