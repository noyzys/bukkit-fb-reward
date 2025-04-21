package dev.nautchkafe.facebook.reward;

import java.util.function.Function;
import java.util.function.Consumer;

interface FacebookMessenger {

    Function<String, Consumer<String>> sendMessage();

    Function<String, Consumer<Consumer<String>>> setupWebhook();
}