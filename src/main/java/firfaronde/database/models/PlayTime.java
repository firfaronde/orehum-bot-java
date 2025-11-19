package firfaronde.database.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.postgresql.util.PGInterval;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static firfaronde.database.Database.executeQueryList;

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

    @Override
    public String toString() {
        return id+" "+" "+" "+tracker+" "+timeSpent.getDays()+"d"+timeSpent.getHours()+"h";
    }
}