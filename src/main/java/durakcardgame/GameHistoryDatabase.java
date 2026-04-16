package durakcardgame;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameHistoryDatabase {
    private static final String DB_URL = "jdbc:sqlite:durak_history.db";

    public GameHistoryDatabase() {
        createTableIfNeeded();
    }

    private void createTableIfNeeded() {
        String sql = """
                CREATE TABLE IF NOT EXISTS game_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_name TEXT NOT NULL,
                    difficulty TEXT NOT NULL,
                    result TEXT NOT NULL,
                    winner INTEGER NOT NULL,
                    played_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveGame(String playerName, String difficulty, int winner) {
        String result = winner == 0 ? "Win" : "Loss";

        String sql = """
                INSERT INTO game_history(player_name, difficulty, result, winner)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerName == null || playerName.isBlank() ? "Player" : playerName);
            ps.setString(2, difficulty == null || difficulty.isBlank() ? "easy" : difficulty);
            ps.setString(3, result);
            ps.setInt(4, winner);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getRecentGames(int limit) {
        List<String> history = new ArrayList<>();

        String sql = """
                SELECT player_name, difficulty, result, played_at
                FROM game_history
                ORDER BY id DESC
                LIMIT ?
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(
                            rs.getString("played_at") + " | " +
                            rs.getString("player_name") + " | " +
                            rs.getString("difficulty") + " | " +
                            rs.getString("result")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }
}