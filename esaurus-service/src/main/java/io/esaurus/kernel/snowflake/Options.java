package io.esaurus.kernel.snowflake;

import java.util.Objects;

public final class Options {
  private final SequenceOverflowStrategy sequenceOverflowStrategy;

  public Options(SequenceOverflowStrategy sequenceOverflowStrategy) {
    this.sequenceOverflowStrategy = Objects.requireNonNull(sequenceOverflowStrategy, "sequenceOverflowStrategy");
  }

  public SequenceOverflowStrategy getSequenceOverflowStrategy() {
    return sequenceOverflowStrategy;
  }

  /**
   * Creates options with default settings.
   * <p>
   * If a sequence overflow occurs, uses spin wait to wait for the next timestamp.
   *
   * @return options
   */
  public static Options createDefault() {
    return new Options(SequenceOverflowStrategy.SPIN_WAIT);
  }

  public enum SequenceOverflowStrategy {
    THROW_EXCEPTION,
    SPIN_WAIT
  }
}
