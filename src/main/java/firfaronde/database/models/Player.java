package firfaronde.database.models;

import java.util.Optional;

import static firfaronde.database.Database.executeQueryAsync;

public class Player {
    public static Optional<String> getCkey(String uuid) {
        return executeQueryAsync(
                "SELECT last_seen_user_name FROM player WHERE user_id::text = ?",
                stmt->stmt.setString(1, uuid),
                rs->rs.getString("last_seen_user_name")
        );
    }

    public static Optional<Boolean> isExists(String ckey) {
        return executeQueryAsync("SELECT EXISTS(SELECT * FROM player WHERE last_seen_user_name = ?)",
                stmt->stmt.setString(1, ckey),
                rs->rs.getBoolean("exists")
        );
    }

    public static Optional<String> getUsid(String ckey) {
        return executeQueryAsync("SELECT user_id FROM player WHERE last_seen_user_name = ?",
                stmt->stmt.setString(1, ckey),
                rs->rs.getString("user_id")
        );
    }
}
