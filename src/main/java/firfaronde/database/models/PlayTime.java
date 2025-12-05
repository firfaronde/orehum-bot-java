package firfaronde.database.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.postgresql.util.PGInterval;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static firfaronde.database.Database.executeQueryList;
import static firfaronde.database.Database.executeUpdate;

@Data
@AllArgsConstructor
public class PlayTime {
    public int id;
    public String userId;
    public String tracker;
    public PGInterval timeSpent;

    public static List<PlayTime> getPlaytime(String ckey) {
        return executeQueryList(
                "SELECT pt.* FROM player p JOIN play_time pt ON pt.player_id = p.user_id WHERE p.last_seen_user_name = ? ORDER BY pt.time_spent DESC LIMIT 10",
                stmt->stmt.setString(1, ckey),
                PlayTime::rsToPlayTime
        );
    }

    public static PlayTime rsToPlayTime(ResultSet rs) throws SQLException {
        return new PlayTime(
                rs.getInt("play_time_id"),
                rs.getString("player_id"),
                rs.getString("tracker"),
                (PGInterval) rs.getObject("time_spent")
        );
    }

    public static boolean addPlaytime(String usid, String tracker, int hours) {
        return executeUpdate(
                "INSERT INTO play_time (player_id, tracker, time_spent) " +
                        "VALUES (?, ?, make_interval(hours => ?)) " +
                        "ON CONFLICT (player_id, tracker) DO UPDATE " +
                        "SET time_spent = play_time.time_spent + make_interval(hours => ?)",
                stmt -> {
                    stmt.setObject(1, UUID.fromString(usid));
                    stmt.setString(2, tracker);
                    stmt.setInt(3, hours);
                    stmt.setInt(4, hours); // update
                }
        );
    }

    @Override
    public String toString() {
        return id+" "+" "+" "+tracker+" "+timeSpent.getDays()+"d"+timeSpent.getHours()+"h";
    }
}