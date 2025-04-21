package dev.nautchkafe.facebook.reward.web;

import com.restfb.JsonMapper;
import com.restfb.types.webhook.messaging.MessagingItem;
import dev.nautchkafe.facebook.reward.FacebookRewardApp;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

final class FacebookWebhookVerticle extends AbstractVerticle {

    private final FacebookRewardApp rewardApp;
    private final String verifyToken;
    private final int port;
    private final JsonMapper jsonMapper;

    FacebookWebhookVerticle(
        final FacebookRewardApp rewardApp,
        final String verifyToken,
        final int port,
        final JsonMapper jsonMapper
    ) {
        this.rewardApp = rewardApp;
        this.verifyToken = verifyToken;
        this.port = port;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void start(final Promise<Void> startPromise) {
        final Router router = Router.router(vertx);
        
        router.route()
            .handler(BodyHandler.create())
            .failureHandler(ctx -> {
                rewardApp.logError("Webhook error: " + ctx.failure().getMessage());
                ctx.response().setStatusCode(500).end();
            });

        router.route(HttpMethod.GET, "/webhook")
            .handler(ctx -> handleVerification(ctx));
        
        router.route(HttpMethod.POST, "/webhook")
            .handler(ctx -> vertx.executeBlocking(
                promise -> {
                    try {
                        processMessage(ctx.getBodyAsString());
                        promise.complete();
                    } catch (Exception e) {
                        promise.fail(e);
                    }
                },
                false,
                res -> ctx.response().end(res.succeeded() ? "OK" : "ERROR")
            ));

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(port)
            .onSuccess(server -> {
                rewardApp.logInfo("Webhook started on port " + port);
                startPromise.complete();
            })
            .onFailure(startPromise::fail);
    }

    private void handleVerification(final RoutingContext ctx) {
        final String mode = ctx.request().getParam("hub.mode");
        final String token = ctx.request().getParam("hub.verify_token");
        final String challenge = ctx.request().getParam("hub.challenge");

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            ctx.response().end(challenge);
        } 
            
        ctx.response().setStatusCode(403).end();
    }

    private void processMessage(final String jsonBody) {
        try {
            final MessagingItem item = jsonMapper.toJavaObject(jsonBody, MessagingItem.class);
            if (item.getMessage() != null && item.getSender() != null) {
                rewardApp.processFacebookMessage(
                    item.getSender().getId(),
                    item.getMessage().getText());
            }
        } catch (final Exception e) {
            rewardApp.logError("Message processing failed: " + e.getMessage());
        }
    }
}