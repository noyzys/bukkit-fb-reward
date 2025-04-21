package dev.nautchkafe.facebook.reward;

import java.util.function.Function;

interface FacebookRewardValidator {

    Function<FacebookBukkitPlayer, Function<Reward, Either<String, Instant>>> validateCooldown();

    Function<String, Either<String, String>> validatePlayerName();

    Function<FacebookPendingReward, Either<String, FacebookPendingReward>> validateReward();
}