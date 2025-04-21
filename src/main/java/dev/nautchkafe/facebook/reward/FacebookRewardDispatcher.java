package dev.nautchkafe.facebook.reward;

import java.util.function.Function;
import java.util.function.Consumer;

interface FacebookRewardDispatcher {

    Function<FacebookRewardClaim, Consumer<Reward>> dispatch();

    Consumer<String> broadcast();

    Consumer<String> executeCommand();
}