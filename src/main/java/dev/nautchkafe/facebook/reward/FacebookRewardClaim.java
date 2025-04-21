package dev.nautchkafe.facebook.reward;

import java.time.Instant;
import java.util.UUID;

record FacebookRewardClaim(
    UUID claimId,
    FacebookPendingReward reward,
    Instant claimTime
) {
}