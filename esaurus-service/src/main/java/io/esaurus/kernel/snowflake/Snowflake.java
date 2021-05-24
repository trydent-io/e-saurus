package io.esaurus.kernel.snowflake;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Snowflake {
  /**
   * Lock for sequence and lastTimestamp.
   */
  private final Object lock = new Object();

  // Stuff which is set in the constructor
  private final long generatorId;
  private final TimeSource timeSource;
  private final Structure structure;
  private final Options options;

  // precalculated variables for bit magic
  private final long maxSequence;
  private final long maskTime;
  private final int shiftTime;
  private final int shiftGenerator;

  /**
   * Tracks the last generated timestamp.
   */
  private long lastTimestamp = -1;
  /**
   * Sequence number, unique per timestamp.
   */
  private long sequence = 0;

  // Structure:
  // time || generator || sequence
  private Snowflake(long generatorId, TimeSource timeSource, Structure structure, Options options) {
    this.timeSource = Objects.requireNonNull(timeSource, "timeSource");
    this.structure = Objects.requireNonNull(structure, "structure");
    this.options = Objects.requireNonNull(options, "options");

    if (generatorId < 0 || generatorId >= structure.maxGenerators()) {
      throw new IllegalArgumentException("generatorId must be between 0 (inclusive) and " + structure.maxGenerators() + " (exclusive), but was " + generatorId);
    }

    this.generatorId = generatorId;

    maskTime = calculateMask(this.structure.timestampBits());
    maxSequence = calculateMask(this.structure.sequenceBits());
    shiftTime = this.structure.generatorBits() + this.structure.sequenceBits();
    shiftGenerator = this.structure.sequenceBits();
  }

  public static Snowflake createCustom(long generatorId, TimeSource timeSource, Structure structure, Options options) {
    return new Snowflake(generatorId, timeSource, structure, options);
  }

  /**
   * Creates a generator with default settings.
   * <p>
   * Uses 2020-01-01T00:00:00Z as epoch, 41 bits for the timestamp, 10 for the generator id and 12 for the sequence. If a
   * sequence overflow occurs, uses spin wait to wait for the next timestamp.
   *
   * @param generatorId id of the generator. Must be unique across all instances
   * @return generator
   */
  public static Snowflake createDefault(int generatorId) {
    return new Snowflake(generatorId, TimeSource.createDefault(), Structure.createDefault(), Options.createDefault());
  }

  public static void main(String[] args) {
    final var snowflake = Snowflake.createDefault(512);
    var now = Instant.now();
    for (int i = 0; i < 100_000_000; i++)
      snowflake.next();
    System.out.println(Instant.now().toEpochMilli() - now.toEpochMilli());

    now = Instant.now();
    for (int i = 0; i < 100_000_000; i++)
      UUID.randomUUID();
    System.out.println(Instant.now().toEpochMilli() - now.toEpochMilli());
  }

  /**
   * Generates the next id.
   *
   * @return next id
   * @throws IllegalStateException if some invariant has been broken, e.g. the clock moved backwards or a sequence overflow occured
   */
  public long next() {
    var ticks = timeSource.ticks();
    if (ticks < 0) {
      throw new IllegalStateException("Clock gave negative ticks");
    }
    var timestamp = ticks & maskTime;

    synchronized (lock) {
      // Guard against non-monotonic clocks
      if (timestamp < lastTimestamp) {
        throw new IllegalStateException("Timestamp moved backwards or wrapped around");
      }

      if (timestamp == lastTimestamp) {
        // Same timeslot
        if (sequence >= maxSequence) {
          handleSequenceOverflow();
          return next();
        }
        sequence++;
      } else {
        // other timeslot, reset sequence
        sequence = 0;
        lastTimestamp = timestamp;
      }

      return (timestamp << shiftTime) + (generatorId << shiftGenerator) + sequence;
    }
  }

  private void handleSequenceOverflow() {
    switch (this.options.getSequenceOverflowStrategy()) {
      case THROW_EXCEPTION -> throw new IllegalStateException("Sequence overflow");
      case SPIN_WAIT -> spinWaitForNextTick(lastTimestamp);
      default -> throw new AssertionError("Unexpected enum value: " + this.options.getSequenceOverflowStrategy());
    }
  }

  private void spinWaitForNextTick(long lastTimestamp) {
    long timestamp;
    do
    {
      Thread.onSpinWait();
      timestamp = timeSource.ticks() & maskTime;
    } while (timestamp == lastTimestamp);
  }

  private long calculateMask(int bits) {
    return (1L << bits) - 1;
  }
}
