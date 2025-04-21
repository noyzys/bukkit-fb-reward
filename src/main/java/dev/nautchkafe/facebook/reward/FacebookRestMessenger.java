package dev.nautchkafe.facebook.reward;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.types.webhook.messaging.MessagingItem;
import com.restfb.webhook.Webhook;
import java.util.function.Function;
import java.util.function.Consumer;

final class FacebookRestMessenger implements FacebookMessenger {

    private final FacebookClient facebookClient;
    private final Webhook webhook;
    private final String pageId;
    private final String verifyToken;

    FacebookRestMessenger(
        final String accessToken,
        final String pageId,
        final String verifyToken
    ) {
        this.facebookClient = new DefaultFacebookClient(accessToken, Version.LATEST);
        this.pageId = pageId;
        this.verifyToken = verifyToken;
        this.webhook = new Webhook();
    }

    @Override
    public Function<String, Consumer<String>> sendMessage() {
        return userId -> message -> {
            try {
                facebookClient.publish(
                    userId + "/messages", 
                    String.class,
                    
                    Parameter.with("message", message),
                    Parameter.with("access_token", facebookClient.getAccessToken())
                );
            } catch (final Exception e) {
                throw new RuntimeException("Failed to send message", e);
            }
        };
    }

    @Override
    public Function<String, Consumer<Consumer<String>>> setupWebhook() {
        return path -> handler -> {
            webhook.registerListener(new Webhook.Listener() {
                // old logic prepare spring, javaling, spark 
                @Override
                public void onMessage(MessagingItem messaging) {
                    if (messaging.getMessage() != null && 
                        messaging.getMessage().getText() != null) {
                        handler.accept(messaging.getMessage().getText());
                    }
                }
            });
        };
    }
}