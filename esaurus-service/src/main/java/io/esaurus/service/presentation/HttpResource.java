package io.esaurus.service.presentation;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.ext.web.RoutingContext;

public interface HttpResource extends Handler<RoutingContext> {

  final class Electricity implements HttpResource {
    private final EventBus bus;
    private final Resources resources;

    @Override
    public void handle(final RoutingContext resource) {
      resource.request().handler(buffer -> bus.publish(address(resource), buffer.toJsonObject(), headers(resource)));
    }

    private DeliveryOptions headers(final RoutingContext resource) {
      return new DeliveryOptions().setHeaders(new HeadersMultiMap().add("resource-id", resource.pathParam("id")));
    }

    private String address(final RoutingContext resource) {
      return "%s-%s".formatted(resource.pathParam("resource"), resource.pathParam("command"));
    }
  }
}
