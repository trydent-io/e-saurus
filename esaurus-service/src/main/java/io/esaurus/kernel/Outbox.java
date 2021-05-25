package io.esaurus.kernel;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import static io.vertx.core.buffer.Buffer.buffer;

public interface Outbox {
  static Outbox event(EventBus bus) {
    return new Event(bus);
  }

  default <R extends Record> Outbox dispatch(String event, R data) {
    return dispatch(event, JsonObject.mapFrom(data).toBuffer());
  }

  default Outbox dispatch(String event, byte[] data) {
    return dispatch(event, buffer(data));
  }

  Outbox dispatch(String event, Buffer data);

  final class Event implements Outbox {
    private final EventBus bus;

    private Event(final EventBus bus) {this.bus = bus;}

    @Override
    public Outbox dispatch(final String event, final Buffer data) {
      bus.publish(event, data);
      return this;
    }
  }
}
