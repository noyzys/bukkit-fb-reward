package dev.nautchkafe.facebook.reward;

import java.util.function.Function;

interface FacebookRewardRepository {

    Function<FacebookPendingReward, Boolean> savePendingReward();

    Function<String, Boolean> hasPendingReward();

    Function<String, Optional<Instant>> findLastRewardTime();

    Function<FacebookRewardClaim, Boolean> saveClaim();
}