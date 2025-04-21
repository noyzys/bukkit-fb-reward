package dev.nautchkafe.facebook.reward;

import io.vertx.core.Vertx;
import java.util.function.BiConsumer;

final class VertxFacebookMessenger implements FacebookMessenger {

    private final Vertx vertx;
    private final BiConsumer<String, String> messageSender;

    public VertxFacebookMessenger(final Vertx vertx, final BiConsumer<String, String> messageSender) {
        this.vertx = vertx;
        this.messageSender = messageSender;
    }

    @Override
    public void sendNotification(final String userId) {
        vertx.eventBus().publish("facebook.messages", userId);
    }

    public void startConsumer() {
        vertx.eventBus().consumer("facebook.messages", msg -> {
            final String userId = msg.body();
            messageSender.accept(userId, "You can claim your reward! Type !reward");
        });
    }
}