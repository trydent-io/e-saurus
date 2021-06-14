package io.esaurus.kernel;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;

import static io.vertx.core.buffer.Buffer.buffer;

public interface Outbox {
  static Outbox event(EventBus bus) {
    return new Event(bus);
  }

  default <R extends Record> Outbox dispatch(String event, R data) {
    final var json = new JsonObject();

    for (final var component : data.getClass().getRecordComponents()) {
      try {
        json.put(component.getName(), component.getAccessor().invoke(data));
      } catch (IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    return dispatch(event, json.toBuffer());
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
