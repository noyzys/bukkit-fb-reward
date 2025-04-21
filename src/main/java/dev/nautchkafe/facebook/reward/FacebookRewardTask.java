package dev.nautchkafe.facebook.reward;

import io.vertx.core.Vertx;
import org.bukkit.Bukkit;
import java.time.Duration;
import java.util.function.BiFunction;

final class FacebookRewardTask {

    private final Vertx vertx;
    private final FacebookRewardRepository repository;
    private final FacebookMessenger messenger;
    private final BiFunction<String, String, Either<String, String>> validator;
    private final Duration interval;

    public FacebookRewardTask(
        final Vertx vertx,
        final FacebookRewardRepository repository,
        final FacebookMessenger messenger,
        final BiFunction<String, String, Either<String, String>> validator,
        final Duration interval
    ) {
        this.vertx = vertx;
        this.repository = repository;
        this.messenger = messenger;
        this.validator = validator;
        this.interval = interval;
    }

    public void start() {
        final long timerId = vertx.setPeriodic(
            interval.toMillis(),
            id -> checkPlayerRewards()
        );
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> 
            vertx.cancelTimer(timerId)
        ));
    }

    private void checkPlayerRewards() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            vertx.executeBlocking(
                promise -> {
                    validator.apply(player.getUniqueId().toString(), player.getName())
                        .flatMap(name -> repository.findLastRewardTime(name))
                        .flatMap(time -> shouldNotify(time) 
                            ? Either.right(player.getUniqueId().toString())
                            : Either.left("Not yet"))
                        .either(
                            error -> promise.complete(),
                            fbId -> {
                                messenger.sendNotification(fbId);
                                promise.complete();
                            }
                        );
                },
                false,
                res -> {}
            );
        });
    }

    private boolean shouldNotify(final Instant lastRewardTime) {
        return Duration.between(lastRewardTime, Instant.now())
            .compareTo(interval.multipliedBy(2)) > 0;
    }
}