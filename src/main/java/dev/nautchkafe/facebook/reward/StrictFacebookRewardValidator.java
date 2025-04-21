package dev.nautchkafe.facebook.reward;

import java.time.Instant;
import java.util.function.Function;

final class StrictFacebookRewardValidator implements FacebookRewardValidator {

    @Override
    public Function<FacebookBukkitPlayer, Function<Reward, Either<String, Instant>>> validateCooldown() {
        return player -> reward -> {
            final Instant nextAvailable = player.lastRewardTime().plus(reward.cooldown());
            return Instant.now().isAfter(nextAvailable)
                ? Either.right(nextAvailable)
                : Either.left("You can claim the reward in " + 
                    Duration.between(Instant.now(), nextAvailable).toHours() + " hours");
        };
    }

    @Override
    public Function<String, Either<String, String>> validatePlayerName() {
        return name -> name.matches("[a-zA-Z0-9_]{3,16}")
            ? Either.right(name)
            : Either.left("Invalid player nickname");
    }

    @Override
    public Function<FacebookPendingReward, Either<String, FacebookPendingReward>> validateReward() {
        return reward -> reward.player() == null || reward.facebookUser() == null
            ? Either.left("Invalid reward data")
            : Either.right(reward);
    }
}