package dev.nautchkafe.facebook.reward;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Consumer;
import org.bukkit.Bukkit;

final class FacebookRewardApp {

    private final FacebookMessenger messenger;
    private final FacebookRewardRepository repository;
    private final FacebookRewardValidator validator;
    private final FacebookRewardDispatcher dispatcher;
    private final FacebookReward rewardConfig;

    FacebookRewardApp(
        final FacebookMessenger messenger,
        final FacebookRewardRepository repository,
        final FacebookRewardValidator validator,
        final FacebookRewardDispatcher dispatcher,
        final FacebookReward rewardConfig
    ) {
        this.messenger = messenger;
        this.repository = repository;
        this.validator = validator;
        this.dispatcher = dispatcher;
        this.rewardConfig = rewardConfig;
    }

    void start() {
        messenger.setupWebhook()
            .apply("/fbwebhook")
            .accept(this::processMessage);
    }

    private void processMessage(final String message) {
        parseCommand(message)
            .flatMap(this::createPendingReward)
            .flatMap(validator.validateReward())
            .flatMap(this::validatePlayerOnline)
            .flatMap(this::checkCooldown)
            .flatMap(this::createClaim)
            .either(
                error -> messenger.sendMessage()
                    .apply("sender_id")
                    .accept(error),
                claim -> {
                    dispatcher.dispatch()
                        .apply(claim)
                        .accept(rewardConfig);
                    repository.saveClaim()
                        .apply(claim);
                }
            );
    }

    private Either<String, Tuple<String, String>> parseCommand(final String message) {
        return message.startsWith("!reward ") && message.split(" ").length == 2
            ? Either.right(Tuple.of("sender_id", message.split(" ")[1]))
            : Either.left("Usage: !reward <nickname>");
    }

    private Either<String, FacebookPendingReward> createPendingReward(final Tuple<String, String> data) {
        return validator.validatePlayerName()
            .apply(data.second())
            .map(playerName -> new FacebookPendingReward(
                UUID.randomUUID(),
                new FacebookUser(data.first(), "Facebook User"),
                new FacebookBukkitPlayer(
                    Bukkit.getPlayerUniqueId(playerName),
                    playerName,
                    repository.findLastRewardTime()
                        .apply(playerName)
                        .orElse(Instant.EPOCH)
                ),
                Instant.now()
            ));
    }

    private Either<String, FacebookPendingReward> validatePlayerOnline(final FacebookPendingReward reward) {
        return Bukkit.getPlayer(reward.player().name()) != null
            ? Either.right(reward)
            : Either.left("Player not found");
    }

    private Either<String, FacebookPendingReward> checkCooldown(final FacebookPendingReward reward) {
        return validator.validateCooldown()
            .apply(reward.player())
            .apply(rewardConfig)
            .map(__ -> reward);
    }

    private Either<String, FacebookRewardClaim> createClaim(final FacebookPendingReward reward) {
        return Either.right(new FacebookRewardClaim(
            UUID.randomUUID(),
            reward,
            Instant.now()
        ));
    }
}