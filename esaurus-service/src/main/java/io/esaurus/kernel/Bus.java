package io.esaurus.kernel;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;

public interface Bus extends Runnable {

  final class RecordCodec implements MessageCodec<Record, Record> {

    @Override
    public void encodeToWire(final Buffer buffer, final Record record) {

    }

    @Override
    public Record decodeFromWire(final int i, final Buffer buffer) {
      return null;
    }

    @Override
    public Record transform(final Record record) {
      return null;
    }

    @Override
    public String name() {
      return null;
    }

    @Override
    public byte systemCodecID() {
      return 0;
    }
  }

  final class Message implements Bus {
    private final EventBus bus;

    private Message(final EventBus bus) {
      this.bus = bus;
    }

    @Override
    public void run() {
      this.bus.registerCodec(new MessageCodec() {
        @Override
        public void encodeToWire(final Buffer buffer, final Object o) {

        }

        @Override
        public Object decodeFromWire(final int i, final Buffer buffer) {
          return null;
        }

        @Override
        public Object transform(final Object o) {
          return null;
        }

        @Override
        public String name() {
          return null;
        }

        @Override
        public byte systemCodecID() {
          return 0;
        }
      })
    }
  }
}
