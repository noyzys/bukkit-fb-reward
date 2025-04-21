package dev.nautchkafe.facebook.reward;

import java.sql.*;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

final class PostgresFacebookRewardRepository implements FacebookRewardRepository {

    private final Supplier<Connection> connectionSupplier;

    PostgresFacebookRewardRepository(
        final String url,
        final String user,
        final String password
    ) {
        this.connectionSupplier = () -> {
            try {
                return DriverManager.getConnection(url, user, password);
            } catch (final SQLException e) {
                throw new RuntimeException("Connection failed", e);
            }
        };
    }

    @Override
    public Function<FacebookPendingReward, Boolean> savePendingReward() {
        return reward -> {
            try (final Connection connection = connectionSupplier.get();
                 final PreparedStatement statment = connection.prepareStatement("""
                     INSERT INTO pending_rewards VALUES (?, ?, ?, ?, ?, ?)
                     """)) {
                
                statment.setObject(1, reward.rewardId());
                statment.setString(2, reward.facebookUser().id());
                statment.setString(3, reward.facebookUser().name());
                statment.setObject(4, reward.player().uuid());
                statment.setString(5, reward.player().name());
                statment.setTimestamp(6, Timestamp.from(reward.requestTime()));
                
                return statment.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Save failed", e);
            }
        };
    }
}