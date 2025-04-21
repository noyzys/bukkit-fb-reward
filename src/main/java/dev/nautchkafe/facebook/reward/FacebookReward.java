package dev.nautchkafe.facebook.reward;

import java.time.Duration;

record FacebookReward(
    String commandTemplate,
    String broadcastMessage,
    Duration cooldown
) {

    String peekCommand(final String playerName) {
        return commandTemplate.replace("%player%", playerName);
    }
}