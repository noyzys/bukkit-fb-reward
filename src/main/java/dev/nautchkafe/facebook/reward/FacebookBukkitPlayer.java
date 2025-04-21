package dev.nautchkafe.facebook.reward;

import java.time.Instant;
import java.util.UUID;

record FacebookBukkitPlayer(
    UUID uuid,
    String name,
    Instant lastRewardTime
) {
}