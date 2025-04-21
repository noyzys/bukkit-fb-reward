package dev.nautchkafe.facebook.reward;

import java.time.Instant;
import java.util.UUID;

record FacebookPendingReward(
    UUID rewardId,
    FacebookUser facebookUser,
    FacebookBukkitPlayer player,
    Instant requestTime
) {
}