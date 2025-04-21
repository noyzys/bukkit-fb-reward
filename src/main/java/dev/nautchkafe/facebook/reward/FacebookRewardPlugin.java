package dev.nautchkafe.facebook.reward;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.time.Duration;
import java.util.function.Consumer;

public final class FacebookRewardPlugin extends JavaPlugin {

    private FacebookRewardApp rewardApp;
    private Vertx vertx;

    @Override
    public void onEnable() {
        vertx = Vertx.vertx();
        final FacebookMessenger messenger = new FacebookRestMessenger(
            getConfig().getString("facebook.token"),
            getConfig().getString("facebook.page_id")
        );

        final FacebookWebhookVerticle webhookVerticle = new FacebookWebhookVerticle(
            rewardApp,
            getConfig().getString("facebook.verify_token"),
            getConfig().getInt("facebook.webhook_port", 8080),
            new DefaultJsonMapper()

        );

        final FacebookRewardRepository repository = new PostgresFacebookRewardRepository(
            getConfig().getString("database.url"),
            getConfig().getString("database.user"),
            getConfig().getString("database.password")
        );

        final FacebookRewardValidator validator = new StrictFacebookRewardValidator();
        final FacebookRewardDispatcher dispatcher = new BukkitRewardDispatcher(
            command -> getServer().dispatchCommand(getServer().getConsoleSender(), command),
            message -> getServer().broadcastMessage(message)
        );

        final FacebookReward rewardConfig = new FacebookReward(
            "give %player% diamond 1",
            "Player %player% reward!",
            Duration.ofHours(24)
        );

        rewardApp = new FacebookRewardApp(
            messenger,
            repository,
            validator,
            dispatcher,
            rewardConfig
        );

        rewardApp.start();

        getServer().getScheduler().runTaskTimerAsynchronously(
            this,
            new FacebookRewardTask(repository, messenger),
            0L,
            20L * 60
        );

        vertx.deployVerticle(webhookVerticle)
            .onSuccess(id -> getLogger().info("Webhook verticle deployed: " + id))
            .onFailure(err -> getLogger().severe("Webhook failed: " + err.getMessage()));
    }

    @Override
    public void onDisable() {
        if (vertx != null) {
            vertx.close()
                .onSuccess(v -> getLogger().info("Vert.x closed"))
                .onFailure(err -> getLogger().warning("Vert.x close error: " + err.getMessage()));
        }
    }
}
