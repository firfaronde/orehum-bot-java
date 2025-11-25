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
}
