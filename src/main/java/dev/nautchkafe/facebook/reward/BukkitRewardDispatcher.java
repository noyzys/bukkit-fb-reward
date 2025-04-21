package dev.nautchkafe.facebook.reward;

import java.util.function.Function;
import java.util.function.Consumer;

final class BukkitRewardDispatcher implements FacebookRewardDispatcher {
    private final Consumer<String> commandExecutor;
    private final Consumer<String> broadcaster;

    BukkitRewardDispatcher(
        final Consumer<String> commandExecutor,
        final Consumer<String> broadcaster
    ) {
        this.commandExecutor = commandExecutor;
        this.broadcaster = broadcaster;
    }

    @Override
    public Function<FacebookRewardClaim, Consumer<Reward>> dispatch() {
        return claim -> reward -> {
            commandExecutor.accept(reward.peekCommand(claim.reward().player().name()));
            broadcaster.accept(reward.broadcastMessage()
                .replace("%player%", claim.reward().player().name()));
        };
    }

    @Override
    public Consumer<String> broadcast() {
        return broadcaster;
    }

    @Override
    public Consumer<String> executeCommand() {
        return commandExecutor;
    }
}